package com.mongkids.domain.review;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.mongkids.domain.member.dto.LoginMemberResponse;
import com.mongkids.domain.member.dto.SocialLoginRequest;
import com.mongkids.domain.member.entity.MemberRole;
import com.mongkids.domain.member.entity.Provider;
import com.mongkids.domain.member.repository.MemberRepository;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.domain.review.entity.ReviewCode;
import com.mongkids.domain.review.repository.ReviewCodeRepository;
import com.mongkids.global.security.JwtProvider;
import com.mongkids.global.security.TokenCookieManager;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminReviewCodeIntegrationTest {

	private static final String URL = "/api/v1/admin/review-codes";
	private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
	private static final LocalDate TODAY = LocalDate.of(2026, 11, 24);

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private ReviewCodeRepository reviewCodeRepository;

	@Autowired
	private JwtProvider jwtProvider;

	@Autowired
	private ObjectMapper objectMapper;

	@TestConfiguration
	static class TestClockConfig {

		@Bean
		@Primary
		Clock testClock() {
			return Clock.fixed(TODAY.atTime(LocalTime.of(9, 0)).atZone(SEOUL).toInstant(), SEOUL);
		}
	}

	@Test
	@DisplayName("관리자가 아니면 코드를 발급·조회·폐기할 수 없다")
	void notAdmin_forbidden() throws Exception {
		Cookie staff = tokenCookie(registerMember("staff", "직원", MemberRole.STAFF));
		String body = singleBody("2026-12-24");

		mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body))
			.andExpect(status().isUnauthorized());
		mockMvc.perform(post(URL).cookie(staff).contentType(MediaType.APPLICATION_JSON).content(body))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.code").value("A002"));
		mockMvc.perform(get(URL).cookie(staff))
			.andExpect(status().isForbidden());

		Long id = createCode(adminCookie(), body).path("id").asLong();
		mockMvc.perform(post(URL + "/" + id + "/revoke").cookie(staff))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("1회용 코드는 사용 횟수 1, 메모 없이 MK-XXXX-XXXX 형식으로 발급한다")
	void createSingle() throws Exception {
		// when: 1회용인데 사용 횟수·메모를 보내도
		JsonNode code = createCode(adminCookie(), """
			{"kind": "SINGLE", "maxUses": 30, "expiresOn": "2026-12-24", "memo": "김지은 어머님"}
			""");

		// then
		assertThat(code.path("code").asString()).matches("MK-[A-HJ-NP-Z2-9]{4}-[A-HJ-NP-Z2-9]{4}");
		assertThat(code.path("kind").asString()).isEqualTo("SINGLE");
		assertThat(code.path("maxUses").asInt()).isEqualTo(1);
		assertThat(code.path("usedCount").asInt()).isZero();
		assertThat(code.path("memo").isNull()).isTrue();
		assertThat(code.path("status").asString()).isEqualTo("ACTIVE");
	}

	@Test
	@DisplayName("묶음 코드는 사용 횟수와 메모를 저장하고, 사용 횟수가 없으면 거부한다")
	void createBundle() throws Exception {
		Cookie admin = adminCookie();

		JsonNode code = createCode(admin, """
			{"kind": "BUNDLE", "maxUses": 30, "expiresOn": "2026-12-24", "memo": "  햇살어린이집 11월 수업 "}
			""");
		assertThat(code.path("maxUses").asInt()).isEqualTo(30);
		assertThat(code.path("memo").asString()).isEqualTo("햇살어린이집 11월 수업");

		mockMvc.perform(post(URL).cookie(admin).contentType(MediaType.APPLICATION_JSON).content("""
				{"kind": "BUNDLE", "expiresOn": "2026-12-24"}
				"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("R006"));
		mockMvc.perform(post(URL).cookie(admin).contentType(MediaType.APPLICATION_JSON).content("""
				{"kind": "BUNDLE", "maxUses": 501, "expiresOn": "2026-12-24"}
				"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("C001"));
	}

	@Test
	@DisplayName("만료일은 오늘부터 1년 안이어야 한다")
	void expiresOnRange() throws Exception {
		Cookie admin = adminCookie();

		createCode(admin, singleBody("2026-11-24"));
		createCode(admin, singleBody("2027-11-24"));
		for (String date : new String[] {"2026-11-23", "2027-11-25"}) {
			mockMvc.perform(post(URL).cookie(admin).contentType(MediaType.APPLICATION_JSON).content(singleBody(date)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("R006"));
		}
	}

	@Test
	@DisplayName("목록은 최근 발급 순이고, 상태를 계산해 사용 가능·끝난 코드로 거른다")
	void listWithStatus() throws Exception {
		// given: 사용 가능 1, 폐기 1, 다 씀 1, 만료 1
		Cookie admin = adminCookie();
		long active = createCode(admin, singleBody("2026-12-24")).path("id").asLong();
		long revoked = createCode(admin, singleBody("2026-12-24")).path("id").asLong();
		mockMvc.perform(post(URL + "/" + revoked + "/revoke").cookie(admin)).andExpect(status().isOk());
		long usedUp = createCode(admin, singleBody("2026-12-24")).path("id").asLong();
		ReflectionTestUtils.setField(reviewCodeRepository.findById(usedUp).orElseThrow(), "usedCount", 1);
		long expired = createCode(admin, singleBody("2026-12-24")).path("id").asLong();
		ReflectionTestUtils.setField(reviewCodeRepository.findById(expired).orElseThrow(), "expiresOn",
			TODAY.minusDays(1));
		reviewCodeRepository.flush();

		// when / then
		mockMvc.perform(get(URL).cookie(admin))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.totalElements").value(4))
			.andExpect(jsonPath("$.data.content[0].id").value(expired))
			.andExpect(jsonPath("$.data.content[0].status").value("EXPIRED"))
			.andExpect(jsonPath("$.data.content[1].status").value("USED_UP"))
			.andExpect(jsonPath("$.data.content[2].status").value("REVOKED"))
			.andExpect(jsonPath("$.data.content[3].status").value("ACTIVE"));
		mockMvc.perform(get(URL).param("active", "true").cookie(admin))
			.andExpect(jsonPath("$.data.totalElements").value(1))
			.andExpect(jsonPath("$.data.content[0].id").value(active));
		mockMvc.perform(get(URL).param("active", "false").cookie(admin))
			.andExpect(jsonPath("$.data.totalElements").value(3));
	}

	@Test
	@DisplayName("사용 가능한 코드만 폐기하고, 없는 코드는 404다")
	void revoke() throws Exception {
		Cookie admin = adminCookie();
		long id = createCode(admin, singleBody("2026-12-24")).path("id").asLong();

		mockMvc.perform(post(URL + "/" + id + "/revoke").cookie(admin))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.status").value("REVOKED"))
			.andExpect(jsonPath("$.data.revokedAt").isNotEmpty());
		ReviewCode saved = reviewCodeRepository.findById(id).orElseThrow();
		assertThat(saved.getRevokedAt()).isNotNull();

		mockMvc.perform(post(URL + "/" + id + "/revoke").cookie(admin))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.error.code").value("R005"));
		mockMvc.perform(post(URL + "/999999/revoke").cookie(admin))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.code").value("R004"));
	}

	private static String singleBody(String expiresOn) {
		return """
			{"kind": "SINGLE", "expiresOn": "%s"}
			""".formatted(expiresOn);
	}

	private JsonNode createCode(Cookie admin, String body) throws Exception {
		String response = mockMvc.perform(post(URL).cookie(admin).contentType(MediaType.APPLICATION_JSON).content(body))
			.andExpect(status().isCreated())
			.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(response).path("data");
	}

	private Cookie adminCookie() {
		return tokenCookie(registerMember("admin", "대표", MemberRole.ADMIN));
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
