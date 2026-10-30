package com.mongkids.domain.member;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mongkids.domain.auth.dto.OAuth2WithdrawPrincipal;
import com.mongkids.domain.auth.handler.OAuth2LoginFailureHandler;
import com.mongkids.domain.auth.handler.OAuth2LoginSuccessHandler;
import com.mongkids.domain.auth.service.SocialUnlinkClient;
import com.mongkids.domain.member.dto.LoginMemberResponse;
import com.mongkids.domain.member.dto.SocialLoginRequest;
import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.entity.Provider;
import com.mongkids.domain.member.repository.MemberRepository;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;
import com.mongkids.global.security.JwtProvider;
import com.mongkids.global.security.TokenCookieManager;

import jakarta.servlet.http.Cookie;

/**
 * 내 정보 (SCR-025): 조회(FR-027), SNS 수신 동의 변경(FR-019), 회원 탈퇴(FR-028).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MyInfoIntegrationTest {

	private static final String SNS_URL = "/api/v1/members/me/sns-agreement";
	private static final String WITHDRAW_CALLBACK = "/login/oauth2/code/kakao-withdraw";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private JwtProvider jwtProvider;

	@Autowired
	private OAuth2LoginSuccessHandler successHandler;

	@Autowired
	private OAuth2LoginFailureHandler failureHandler;

	@MockitoBean
	private SocialUnlinkClient socialUnlinkClient;

	@Test
	@DisplayName("내 정보는 전화번호·SNS 수신 동의·가입일까지 돌려준다")
	void getMyInfo_returnsProfile() throws Exception {
		// given
		LoginMemberResponse member = register("12345", "몽키");
		memberRepository.findById(member.id()).orElseThrow().registerSignUpInfo("01012345678", true);

		// when & then
		mockMvc.perform(get("/api/v1/members/me").cookie(tokenCookie(member)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.name").value("몽키"))
			.andExpect(jsonPath("$.data.email").value("monkey@kakao.com"))
			.andExpect(jsonPath("$.data.phone").value("01012345678"))
			.andExpect(jsonPath("$.data.snsAgreed").value(true))
			.andExpect(jsonPath("$.data.snsAgreedAt").isNotEmpty())
			.andExpect(jsonPath("$.data.createdAt").isNotEmpty());
	}

	@Test
	@DisplayName("소셜 로그인에서 받은 이메일·휴대폰 번호를 가입 때 저장하고, 다음 로그인에서 받은 값으로 갱신한다")
	void login_savesSocialContact() {
		// given: 처음 로그인 (가입)
		LoginMemberResponse member = memberService.loginOrRegister(
			new SocialLoginRequest(Provider.NAVER, "naver-id", "몽키", "first@naver.com", "01012345678"));
		Member saved = memberRepository.findById(member.id()).orElseThrow();
		assertThat(saved.getEmail()).isEqualTo("first@naver.com");
		assertThat(saved.getPhone()).isEqualTo("01012345678");

		// when: 번호가 바뀐 뒤 로그인, 이메일 동의는 철회
		memberService.loginOrRegister(
			new SocialLoginRequest(Provider.NAVER, "naver-id", "몽키", null, "01099998888"));

		// then
		Member updated = memberRepository.findById(member.id()).orElseThrow();
		assertThat(updated.getPhone()).isEqualTo("01099998888");
		assertThat(updated.getEmail()).isEqualTo("first@naver.com");
	}

	@Test
	@DisplayName("SNS 수신 동의를 바꾸면 동의 시각을 저장하고, 철회하면 지운다")
	void changeSnsAgreement() throws Exception {
		Cookie cookie = tokenCookie(register("12345", "몽키"));

		mockMvc.perform(put(SNS_URL).cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("""
				{"agreed": true}
				"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.snsAgreed").value(true))
			.andExpect(jsonPath("$.data.snsAgreedAt").isNotEmpty());

		mockMvc.perform(put(SNS_URL).cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("""
				{"agreed": false}
				"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.snsAgreed").value(false))
			.andExpect(jsonPath("$.data.snsAgreedAt").value(nullValue()));
	}

	@Test
	@DisplayName("SNS 수신 동의 변경은 로그인이 필요하고 동의 값이 없으면 거부한다")
	void changeSnsAgreement_invalid() throws Exception {
		mockMvc.perform(put(SNS_URL).contentType(MediaType.APPLICATION_JSON).content("""
				{"agreed": true}
				"""))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("A001"));

		mockMvc.perform(put(SNS_URL).cookie(tokenCookie(register("12345", "몽키")))
				.contentType(MediaType.APPLICATION_JSON).content("{}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("C001"));
	}

	@Test
	@DisplayName("탈퇴용 재로그인 시작 주소는 강제 재인증 파라미터를 붙여 인가 페이지로 보낸다")
	void withdrawAuthorization_forcesReauthentication() throws Exception {
		mockMvc.perform(get("/oauth2/authorization/kakao-withdraw"))
			.andExpect(status().is3xxRedirection())
			.andExpect(header().string(HttpHeaders.LOCATION, allOf(
				startsWith("https://kauth.kakao.com/oauth/authorize"),
				containsString("prompt=login"),
				containsString("kakao-withdraw"))));

		mockMvc.perform(get("/oauth2/authorization/naver-withdraw"))
			.andExpect(status().is3xxRedirection())
			.andExpect(header().string(HttpHeaders.LOCATION, allOf(
				startsWith("https://nid.naver.com/oauth2.0/authorize"),
				containsString("auth_type=reauthenticate"))));

		// 일반 로그인에는 붙이지 않는다
		mockMvc.perform(get("/oauth2/authorization/kakao"))
			.andExpect(header().string(HttpHeaders.LOCATION, not(containsString("prompt=login"))));
	}

	@Test
	@DisplayName("본인 확인 후 탈퇴하면 연결을 끊고 개인정보를 지우며, 같은 계정으로 다시 가입하면 새 회원이 된다")
	void withdraw_success() throws Exception {
		// given
		LoginMemberResponse member = register("12345", "몽키");
		memberRepository.findById(member.id()).orElseThrow().registerSignUpInfo("01012345678", true);

		// when
		MockHttpServletResponse response = withdraw(member, "12345");

		// then
		assertThat(response.getRedirectedUrl()).isEqualTo("/?withdrawn=true");
		assertThat(response.getCookie(TokenCookieManager.COOKIE_NAME).getMaxAge()).isZero();
		then(socialUnlinkClient).should().unlink(Provider.KAKAO, "social-access-token");

		Member withdrawn = memberRepository.findById(member.id()).orElseThrow();
		assertThat(withdrawn.isWithdrawn()).isTrue();
		assertThat(withdrawn.getName()).isEqualTo(Member.WITHDRAWN_NAME);
		assertThat(withdrawn.getProviderId()).isNull();
		assertThat(withdrawn.getEmail()).isNull();
		assertThat(withdrawn.getPhone()).isNull();
		assertThat(withdrawn.isSnsAgreed()).isFalse();
		assertThat(withdrawn.getSnsAgreedAt()).isNull();

		LoginMemberResponse rejoined = register("12345", "몽키");
		assertThat(rejoined.id()).isNotEqualTo(member.id());
	}

	@Test
	@DisplayName("다른 소셜 계정으로 본인 확인하면 연결을 끊지 않고 탈퇴하지 않는다")
	void withdraw_accountMismatch() throws Exception {
		LoginMemberResponse member = register("12345", "몽키");

		MockHttpServletResponse response = withdraw(member, "99999");

		assertThat(response.getRedirectedUrl()).isEqualTo("/me?withdrawError=M005");
		then(socialUnlinkClient).shouldHaveNoInteractions();
		assertThat(memberRepository.findById(member.id()).orElseThrow().isWithdrawn()).isFalse();
	}

	@Test
	@DisplayName("직원·관리자는 탈퇴할 수 없다")
	void withdraw_staff_notAllowed() throws Exception {
		LoginMemberResponse member = register("12345", "몽키");
		memberRepository.findById(member.id()).orElseThrow().grantStaff();

		MockHttpServletResponse response = withdraw(member, "12345");

		assertThat(response.getRedirectedUrl()).isEqualTo("/me?withdrawError=M004");
		then(socialUnlinkClient).shouldHaveNoInteractions();
		assertThat(memberRepository.findById(member.id()).orElseThrow().isWithdrawn()).isFalse();
	}

	// 파기를 먼저 flush하고 연결 끊기가 실패하면 롤백되는지 보려면 테스트 트랜잭션 밖에서 실제로 커밋·롤백돼야 한다
	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	@DisplayName("소셜 연결 끊기가 실패하면 파기를 되돌려 개인정보를 남기고 탈퇴를 중단한다")
	void withdraw_unlinkFailed_rollsBack() throws Exception {
		LoginMemberResponse member = register("unlink-fail", "몽키");
		try {
			willThrow(new BusinessException(ErrorCode.SOCIAL_UNLINK_FAILED))
				.given(socialUnlinkClient).unlink(any(), any());
			MockHttpServletResponse response = withdraw(member, "unlink-fail");

			assertThat(response.getRedirectedUrl()).isEqualTo("/me?withdrawError=M006");
			assertThat(response.getCookie(TokenCookieManager.COOKIE_NAME)).isNull();
			assertKept(member.id(), "unlink-fail");

			// 예상하지 못한 오류도 오류 화면 대신 내 정보로 돌려보내고 되돌린다
			willThrow(new IllegalStateException("network")).given(socialUnlinkClient).unlink(any(), any());
			assertThat(withdraw(member, "unlink-fail").getRedirectedUrl()).isEqualTo("/me?withdrawError=failed");
			assertKept(member.id(), "unlink-fail");
		} finally {
			memberRepository.deleteById(member.id());
		}
	}

	private void assertKept(Long memberId, String providerId) {
		Member kept = memberRepository.findById(memberId).orElseThrow();
		assertThat(kept.isWithdrawn()).isFalse();
		assertThat(kept.getProviderId()).isEqualTo(providerId);
		assertThat(kept.getEmail()).isEqualTo("monkey@kakao.com");
	}

	@Test
	@DisplayName("로그인 쿠키 없이 탈퇴 콜백이 오면 탈퇴하지 않는다")
	void withdraw_withoutLogin() throws Exception {
		LoginMemberResponse member = register("12345", "몽키");
		MockHttpServletRequest request = new MockHttpServletRequest("GET", WITHDRAW_CALLBACK);
		MockHttpServletResponse response = new MockHttpServletResponse();

		successHandler.onAuthenticationSuccess(request, response,
			new TestingAuthenticationToken(withdrawPrincipal("12345"), null));

		assertThat(response.getRedirectedUrl()).isEqualTo("/me?withdrawError=A001");
		assertThat(memberRepository.findById(member.id()).orElseThrow().isWithdrawn()).isFalse();
	}

	@Test
	@DisplayName("탈퇴용 재로그인을 취소하면 내 정보 화면으로 돌려보내고, 일반 로그인 실패는 기존 주소로 보낸다")
	void withdrawFailure_redirectsToMyInfo() throws Exception {
		OAuth2AuthenticationException cancelled =
			new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.ACCESS_DENIED));

		MockHttpServletResponse withdrawResponse = new MockHttpServletResponse();
		failureHandler.onAuthenticationFailure(new MockHttpServletRequest("GET", WITHDRAW_CALLBACK),
			withdrawResponse, cancelled);
		assertThat(withdrawResponse.getRedirectedUrl()).isEqualTo("/me?withdrawError=cancelled");

		MockHttpServletResponse loginResponse = new MockHttpServletResponse();
		failureHandler.onAuthenticationFailure(new MockHttpServletRequest("GET", "/login/oauth2/code/kakao"),
			loginResponse, cancelled);
		assertThat(loginResponse.getRedirectedUrl()).isEqualTo("/?loginError=true");
	}

	private MockHttpServletResponse withdraw(LoginMemberResponse member, String reauthProviderId) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", WITHDRAW_CALLBACK);
		request.setCookies(tokenCookie(member));
		MockHttpServletResponse response = new MockHttpServletResponse();
		successHandler.onAuthenticationSuccess(request, response,
			new TestingAuthenticationToken(withdrawPrincipal(reauthProviderId), null));
		return response;
	}

	private static OAuth2WithdrawPrincipal withdrawPrincipal(String providerId) {
		return new OAuth2WithdrawPrincipal("kakao-withdraw", Provider.KAKAO, providerId, "social-access-token",
			Map.of());
	}

	private LoginMemberResponse register(String providerId, String name) {
		return memberService.loginOrRegister(
			new SocialLoginRequest(Provider.KAKAO, providerId, name, "monkey@kakao.com"));
	}

	private Cookie tokenCookie(LoginMemberResponse member) {
		return new Cookie(TokenCookieManager.COOKIE_NAME, jwtProvider.createToken(member.id(), member.role().name()));
	}
}
