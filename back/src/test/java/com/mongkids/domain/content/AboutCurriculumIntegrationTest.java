package com.mongkids.domain.content;

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
class AboutCurriculumIntegrationTest {

	private static final String URL = "/api/v1/about-curriculum-months";
	private static final String ADMIN_URL = "/api/v1/admin/about-curriculum-months";
	private static final String THREE_MONTHS = """
		{"months": [
			{"month": " 3월 ", "topic": "기본 움직임 · 균형 잡기", "goal": " 몸의 중심 잡기 "},
			{"month": "4월", "topic": "공 던지고 받기", "goal": "눈과 손 협응"},
			{"month": "5월", "topic": "협동 놀이 · 규칙 지키기", "goal": "친구와 차례 지키기"}
		]}
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
	@DisplayName("저장 값이 없으면 로그인 없이 빈 목록을 받는다")
	void get_empty() throws Exception {
		mockMvc.perform(get(URL))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(0));
	}

	@Test
	@DisplayName("관리자가 아니면 저장·되돌리기를 할 수 없다")
	void notAdmin_forbidden() throws Exception {
		Cookie staff = tokenCookie(registerMember("staff", "직원", MemberRole.STAFF));

		mockMvc.perform(put(ADMIN_URL).contentType(MediaType.APPLICATION_JSON).content(THREE_MONTHS))
			.andExpect(status().isUnauthorized());

		mockMvc.perform(put(ADMIN_URL).cookie(staff).contentType(MediaType.APPLICATION_JSON).content(THREE_MONTHS))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.code").value("A002"));

		mockMvc.perform(delete(ADMIN_URL).cookie(staff))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("관리자가 저장하면 앞뒤 공백을 지워 순서대로 보여 주고, 다시 저장하면 바뀌고, 되돌리면 빈 목록이 된다")
	void save_update_reset() throws Exception {
		// given
		Cookie admin = tokenCookie(registerMember("admin", "대표", MemberRole.ADMIN));

		// when
		mockMvc.perform(put(ADMIN_URL).cookie(admin).contentType(MediaType.APPLICATION_JSON).content(THREE_MONTHS))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].month").value("3월"))
			.andExpect(jsonPath("$.data[0].goal").value("몸의 중심 잡기"));

		// then
		mockMvc.perform(get(URL))
			.andExpect(jsonPath("$.data.length()").value(3))
			.andExpect(jsonPath("$.data[0].month").value("3월"))
			.andExpect(jsonPath("$.data[2].topic").value("협동 놀이 · 규칙 지키기"))
			.andExpect(jsonPath("$.data[2].goal").value("친구와 차례 지키기"));

		mockMvc.perform(put(ADMIN_URL).cookie(admin).contentType(MediaType.APPLICATION_JSON).content("""
				{"months": [
					{"month": "9월", "topic": "가을 운동회 준비", "goal": "팀워크"},
					{"month": "10월", "topic": "릴레이 달리기", "goal": "순발력"},
					{"month": "11월", "topic": "줄넘기", "goal": "리듬감"}
				]}
				"""))
			.andExpect(status().isOk());

		mockMvc.perform(get(URL))
			.andExpect(jsonPath("$.data.length()").value(3))
			.andExpect(jsonPath("$.data[1].month").value("10월"))
			.andExpect(jsonPath("$.data[1].goal").value("순발력"));

		mockMvc.perform(delete(ADMIN_URL).cookie(admin))
			.andExpect(status().isOk());

		mockMvc.perform(get(URL))
			.andExpect(jsonPath("$.data.length()").value(0));
	}

	@Test
	@DisplayName("3줄이 아니거나 빈 값·목표 누락·길이 초과가 있으면 저장하지 않는다")
	void invalid_rejected() throws Exception {
		Cookie admin = tokenCookie(registerMember("admin", "대표", MemberRole.ADMIN));

		String[] invalidBodies = {
			"""
				{"months": [{"month": "3월", "topic": "공 놀이", "goal": "목표"}, {"month": "4월", "topic": "균형", "goal": "목표"}]}
				""",
			"""
				{"months": [{"month": " ", "topic": "공 놀이", "goal": "목표"}, {"month": "4월", "topic": "균형", "goal": "목표"},
					{"month": "5월", "topic": "협동", "goal": "목표"}]}
				""",
			"""
				{"months": [{"month": "3월부터 4월까지의 기간", "topic": "공 놀이", "goal": "목표"}, {"month": "4월", "topic": "균형", "goal": "목표"},
					{"month": "5월", "topic": "협동", "goal": "목표"}]}
				""",
			"""
				{"months": [{"month": "3월", "topic": "%s", "goal": "목표"}, {"month": "4월", "topic": "균형", "goal": "목표"},
					{"month": "5월", "topic": "협동", "goal": "목표"}]}
				""".formatted("가".repeat(41)),
			"""
				{"months": [{"month": "3월", "topic": "공 놀이"}, {"month": "4월", "topic": "균형", "goal": "목표"},
					{"month": "5월", "topic": "협동", "goal": "목표"}]}
				""",
			"""
				{"months": [{"month": "3월", "topic": "공 놀이", "goal": "%s"}, {"month": "4월", "topic": "균형", "goal": "목표"},
					{"month": "5월", "topic": "협동", "goal": "목표"}]}
				""".formatted("가".repeat(41)),
			"""
				{"months": null}
				"""
		};
		for (String body : invalidBodies) {
			mockMvc.perform(put(ADMIN_URL).cookie(admin).contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("C001"));
		}

		mockMvc.perform(get(URL))
			.andExpect(jsonPath("$.data.length()").value(0));
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
