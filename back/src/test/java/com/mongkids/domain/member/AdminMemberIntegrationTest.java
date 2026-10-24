package com.mongkids.domain.member;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.mongkids.domain.member.dto.LoginMemberResponse;
import com.mongkids.domain.member.dto.SocialLoginRequest;
import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.entity.MemberRole;
import com.mongkids.domain.member.entity.Provider;
import com.mongkids.domain.member.repository.MemberRepository;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.global.security.JwtProvider;
import com.mongkids.global.security.TokenCookieManager;
import com.mongkids.global.storage.FileStorage;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminMemberIntegrationTest {

	private static final String URL = "/api/v1/admin/members";

	private static final String POST_BODY = """
		{"category": "NEWS", "title": "가을 운동회", "content": "<p>본문</p>"}
		""";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private JwtProvider jwtProvider;

	@MockitoBean
	private FileStorage fileStorage;

	@Test
	@DisplayName("관리자가 아니면 회원 목록을 보거나 권한을 바꿀 수 없다")
	void notAdmin_forbidden() throws Exception {
		LoginMemberResponse staff = registerMember("staff", "직원", MemberRole.STAFF);
		LoginMemberResponse member = registerMember("member", "회원", MemberRole.MEMBER);

		mockMvc.perform(get(URL).cookie(tokenCookie(staff)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.code").value("A002"));

		mockMvc.perform(put(URL + "/{id}/role", member.id()).cookie(tokenCookie(staff))
				.contentType(MediaType.APPLICATION_JSON).content("""
					{"role": "STAFF"}
					"""))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("회원 목록은 탈퇴 회원을 빼고 권한·이름·전화번호로 거른다")
	void searchMembers() throws Exception {
		// given
		Cookie admin = tokenCookie(registerMember("admin", "대표", MemberRole.ADMIN));
		registerMember("staff", "한도윤", MemberRole.STAFF);
		LoginMemberResponse kim = registerMember("kim", "김지은", MemberRole.MEMBER);
		memberRepository.findById(kim.id()).orElseThrow().registerSignUpInfo("01012345678", false);
		Member withdrawn = memberRepository.findById(registerMember("gone", "김탈퇴", MemberRole.MEMBER).id())
			.orElseThrow();
		withdrawn.withdraw();

		// when & then
		mockMvc.perform(get(URL).cookie(admin))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.totalElements").value(3));

		mockMvc.perform(get(URL).param("role", "STAFF").cookie(admin))
			.andExpect(jsonPath("$.data.totalElements").value(1))
			.andExpect(jsonPath("$.data.content[0].name").value("한도윤"));

		mockMvc.perform(get(URL).param("keyword", "김").cookie(admin))
			.andExpect(jsonPath("$.data.totalElements").value(1))
			.andExpect(jsonPath("$.data.content[0].name").value("김지은"));

		mockMvc.perform(get(URL).param("keyword", "1234-5678").cookie(admin))
			.andExpect(jsonPath("$.data.totalElements").value(1))
			.andExpect(jsonPath("$.data.content[0].phone").value("01012345678"));
	}

	@Test
	@DisplayName("직원으로 지정하면 게시물을 쓸 수 있고, 회수하면 다시 쓸 수 없다")
	void grantAndRevokeStaff() throws Exception {
		// given
		Cookie admin = tokenCookie(registerMember("admin", "대표", MemberRole.ADMIN));
		LoginMemberResponse member = registerMember("member", "회원", MemberRole.MEMBER);

		// when & then
		mockMvc.perform(put(URL + "/{id}/role", member.id()).cookie(admin)
				.contentType(MediaType.APPLICATION_JSON).content("""
					{"role": "STAFF"}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.role").value("STAFF"));

		mockMvc.perform(post("/api/v1/posts").cookie(tokenCookie(member))
				.contentType(MediaType.APPLICATION_JSON).content(POST_BODY))
			.andExpect(status().isCreated());

		mockMvc.perform(put(URL + "/{id}/role", member.id()).cookie(admin)
				.contentType(MediaType.APPLICATION_JSON).content("""
					{"role": "MEMBER"}
					"""))
			.andExpect(jsonPath("$.data.role").value("MEMBER"));

		mockMvc.perform(post("/api/v1/posts").cookie(tokenCookie(member))
				.contentType(MediaType.APPLICATION_JSON).content(POST_BODY))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("관리자의 역할은 바꿀 수 없고 관리자 권한을 줄 수도 없다")
	void adminRole_unchangeable() throws Exception {
		LoginMemberResponse admin = registerMember("admin", "대표", MemberRole.ADMIN);
		LoginMemberResponse member = registerMember("member", "회원", MemberRole.MEMBER);

		mockMvc.perform(put(URL + "/{id}/role", admin.id()).cookie(tokenCookie(admin))
				.contentType(MediaType.APPLICATION_JSON).content("""
					{"role": "MEMBER"}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("M003"));

		mockMvc.perform(put(URL + "/{id}/role", member.id()).cookie(tokenCookie(admin))
				.contentType(MediaType.APPLICATION_JSON).content("""
					{"role": "ADMIN"}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("M003"));

		mockMvc.perform(put(URL + "/{id}/role", 9999L).cookie(tokenCookie(admin))
				.contentType(MediaType.APPLICATION_JSON).content("""
					{"role": "STAFF"}
					"""))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.code").value("M001"));
	}

	private Cookie tokenCookie(LoginMemberResponse member) {
		return new Cookie(TokenCookieManager.COOKIE_NAME, jwtProvider.createToken(member.id(), member.role().name()));
	}

	private LoginMemberResponse registerMember(String providerId, String name, MemberRole role) {
		LoginMemberResponse member = memberService.loginOrRegister(
			new SocialLoginRequest(Provider.KAKAO, providerId, name, null));
		ReflectionTestUtils.setField(memberRepository.findById(member.id()).orElseThrow(), "role", role);
		return member;
	}
}
