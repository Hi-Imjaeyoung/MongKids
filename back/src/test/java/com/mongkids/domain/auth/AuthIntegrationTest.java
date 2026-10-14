package com.mongkids.domain.auth;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.mongkids.domain.auth.dto.OAuth2MemberPrincipal;
import com.mongkids.domain.auth.handler.OAuth2LoginSuccessHandler;
import com.mongkids.domain.member.dto.LoginMemberResponse;
import com.mongkids.domain.member.dto.SocialLoginRequest;
import com.mongkids.domain.member.entity.Provider;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.global.security.JwtProvider;
import com.mongkids.global.security.TokenCookieManager;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberService memberService;

	@Autowired
	private JwtProvider jwtProvider;

	@Autowired
	private OAuth2LoginSuccessHandler successHandler;

	@Test
	@DisplayName("소셜 로그인 시작 주소는 카카오·네이버 인가 페이지로 리다이렉트한다")
	void authorization_redirectsToProvider() throws Exception {
		mockMvc.perform(get("/oauth2/authorization/kakao"))
			.andExpect(status().is3xxRedirection())
			.andExpect(header().string(HttpHeaders.LOCATION,
				startsWith("https://kauth.kakao.com/oauth/authorize")));

		mockMvc.perform(get("/oauth2/authorization/naver"))
			.andExpect(status().is3xxRedirection())
			.andExpect(header().string(HttpHeaders.LOCATION,
				startsWith("https://nid.naver.com/oauth2.0/authorize")));
	}

	@Test
	@DisplayName("로그인 성공 처리는 HttpOnly 토큰 쿠키를 발급하고 프론트로 리다이렉트한다")
	void loginSuccess_issuesTokenCookie() throws Exception {
		// given
		LoginMemberResponse member = registerMember();
		OAuth2MemberPrincipal principal = new OAuth2MemberPrincipal(member, Map.of());
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		// when
		successHandler.onAuthenticationSuccess(request, response, new TestingAuthenticationToken(principal, null));

		// then
		assertThat(response.getRedirectedUrl()).isEqualTo("/");
		String setCookie = response.getHeader(HttpHeaders.SET_COOKIE);
		assertThat(setCookie).startsWith(TokenCookieManager.COOKIE_NAME + "=")
			.contains("HttpOnly", "SameSite=Lax", "Path=/");
		String token = response.getCookie(TokenCookieManager.COOKIE_NAME).getValue();
		assertThat(jwtProvider.getAuthentication(token).getPrincipal()).isEqualTo(member.id());
	}

	@Test
	@DisplayName("토큰 쿠키가 있으면 내 정보를 조회한다")
	void getMyInfo_withToken() throws Exception {
		// given
		LoginMemberResponse member = registerMember();
		Cookie cookie = tokenCookie(jwtProvider.createToken(member.id(), member.role().name()));

		// when & then
		mockMvc.perform(get("/api/v1/members/me").cookie(cookie))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.id").value(member.id()))
			.andExpect(jsonPath("$.data.provider").value("KAKAO"))
			.andExpect(jsonPath("$.data.name").value("몽키"))
			.andExpect(jsonPath("$.data.role").value("MEMBER"));
	}

	@Test
	@DisplayName("토큰이 없거나 유효하지 않으면 내 정보 조회는 401을 응답한다")
	void getMyInfo_withoutToken_unauthorized() throws Exception {
		mockMvc.perform(get("/api/v1/members/me"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.error.code").value("A001"));

		mockMvc.perform(get("/api/v1/members/me").cookie(tokenCookie("invalid-token")))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("A001"));
	}

	@Test
	@DisplayName("로그아웃하면 토큰 쿠키를 삭제한다")
	void logout_removesTokenCookie() throws Exception {
		mockMvc.perform(post("/api/v1/auth/logout"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(cookie().maxAge(TokenCookieManager.COOKIE_NAME, 0));
	}

	private LoginMemberResponse registerMember() {
		return memberService.loginOrRegister(new SocialLoginRequest(Provider.KAKAO, "12345", "몽키", null));
	}

	private static Cookie tokenCookie(String token) {
		return new Cookie(TokenCookieManager.COOKIE_NAME, token);
	}
}
