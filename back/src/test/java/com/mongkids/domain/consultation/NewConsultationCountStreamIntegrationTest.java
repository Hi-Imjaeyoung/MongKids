package com.mongkids.domain.consultation;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.mongkids.domain.consultation.dto.ChildAgeGroup;
import com.mongkids.domain.consultation.dto.InstitutionConsultationRequest;
import com.mongkids.domain.consultation.entity.InstitutionConsultation;
import com.mongkids.domain.consultation.repository.ConsultationRepository;
import com.mongkids.domain.consultation.service.ConsultationService;
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

// 커밋 뒤 이벤트(AFTER_COMMIT)를 확인해야 하므로 테스트 트랜잭션을 쓰지 않고, 끝나면 데이터를 지운다
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NewConsultationCountStreamIntegrationTest {

	private static final String STREAM_URL = "/api/v1/admin/consultations/new-count/stream";
	private static final String INSTITUTION_URL = "/api/v1/institution-consultations";
	private static final String EVENT = "event:new-count";
	private static final long WAIT_MILLIS = 3_000;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private ConsultationRepository consultationRepository;

	@Autowired
	private ConsultationService consultationService;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	private JwtProvider jwtProvider;

	@MockitoBean
	private FileStorage fileStorage;

	@AfterEach
	void cleanUp() {
		consultationRepository.deleteAll();
		memberRepository.deleteAll();
	}

	@Test
	@DisplayName("관리자만 구독할 수 있다")
	void subscribe_adminOnly() throws Exception {
		mockMvc.perform(get(STREAM_URL).accept(MediaType.TEXT_EVENT_STREAM))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("A001"));

		mockMvc.perform(get(STREAM_URL).accept(MediaType.TEXT_EVENT_STREAM)
				.cookie(tokenCookie(registerMember("member", MemberRole.MEMBER))))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.code").value("A002"));

		mockMvc.perform(get(STREAM_URL).accept(MediaType.TEXT_EVENT_STREAM)
				.cookie(tokenCookie(registerMember("staff", MemberRole.STAFF))))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.code").value("A002"));
	}

	@Test
	@DisplayName("연결하면 바로 현재 신규 건수를 받는다")
	void subscribe_sendsInitialCount() throws Exception {
		// given
		saveInstitution("햇살어린이집");
		saveInstitution("달빛어린이집");

		// when
		MvcResult result = subscribe();

		// then
		assertThat(result.getResponse().getContentType()).startsWith(MediaType.TEXT_EVENT_STREAM_VALUE);
		assertThat(content(result)).contains(EVENT, "data:2");
	}

	@Test
	@DisplayName("신청이 커밋되면 늘어난 건수를, 처리 상태를 바꾸면 줄어든 건수를 받는다")
	void countChanges_afterCommit() throws Exception {
		// given
		MvcResult result = subscribe();
		assertThat(content(result)).contains("data:0");

		// when: 신청
		mockMvc.perform(post(INSTITUTION_URL).contentType(MediaType.APPLICATION_JSON).content(institutionBody()))
			.andExpect(status().isCreated());

		// then
		assertThat(awaitContent(result, "data:1")).contains("data:1");

		// when: 처리 상태 변경
		long id = consultationRepository.findAll().get(0).getId();
		mockMvc.perform(put("/api/v1/admin/consultations/{id}/status", id).cookie(adminCookie(result))
				.contentType(MediaType.APPLICATION_JSON).content("""
					{"status": "CONTACTED"}
					"""))
			.andExpect(status().isOk());

		// then
		String content = awaitEventCount(result, 3);
		assertThat(content.substring(content.lastIndexOf(EVENT))).contains("data:0");
	}

	@Test
	@DisplayName("신청이 롤백되면 건수를 보내지 않는다")
	void rollback_sendsNothing() throws Exception {
		// given
		MvcResult result = subscribe();

		// when
		transactionTemplate.executeWithoutResult(status -> {
			consultationService.createInstitutionConsultation(institutionRequest());
			status.setRollbackOnly();
		});

		// then
		Thread.sleep(1_500);
		assertThat(eventCount(content(result))).isEqualTo(1);
		assertThat(consultationRepository.count()).isZero();
	}

	@Test
	@DisplayName("짧은 시간에 여러 건이 들어오면 묶어서 보낸다")
	void burst_isBatched() throws Exception {
		// given
		MvcResult result = subscribe();

		// when
		for (int i = 0; i < 5; i++) {
			mockMvc.perform(post(INSTITUTION_URL).contentType(MediaType.APPLICATION_JSON).content(institutionBody()))
				.andExpect(status().isCreated());
		}

		// then: 초기 건수 1회 + 묶어 보낸 전송 2회 이하, 마지막 값은 5
		String content = awaitContent(result, "data:5");
		Thread.sleep(1_500);
		content = content(result);
		assertThat(content.substring(content.lastIndexOf(EVENT))).contains("data:5");
		assertThat(eventCount(content)).isBetween(2, 3);
	}

	private MvcResult subscribe() throws Exception {
		Cookie admin = tokenCookie(registerMember("admin", MemberRole.ADMIN));
		MvcResult result = mockMvc.perform(get(STREAM_URL).accept(MediaType.TEXT_EVENT_STREAM).cookie(admin))
			.andExpect(status().isOk())
			.andExpect(request().asyncStarted())
			.andReturn();
		result.getRequest().setAttribute("adminCookie", admin);
		return result;
	}

	private Cookie adminCookie(MvcResult result) {
		return (Cookie)result.getRequest().getAttribute("adminCookie");
	}

	private String content(MvcResult result) throws Exception {
		return result.getResponse().getContentAsString(StandardCharsets.UTF_8);
	}

	private String awaitContent(MvcResult result, String expected) throws Exception {
		long deadline = System.currentTimeMillis() + WAIT_MILLIS;
		while (!content(result).contains(expected) && System.currentTimeMillis() < deadline) {
			Thread.sleep(50);
		}
		return content(result);
	}

	private String awaitEventCount(MvcResult result, int count) throws Exception {
		long deadline = System.currentTimeMillis() + WAIT_MILLIS;
		while (eventCount(content(result)) < count && System.currentTimeMillis() < deadline) {
			Thread.sleep(50);
		}
		return content(result);
	}

	private int eventCount(String content) {
		return content.split(EVENT, -1).length - 1;
	}

	private void saveInstitution(String name) {
		consultationRepository.save(InstitutionConsultation.builder()
			.phone("0212345678")
			.privacyAgreedAt(LocalDateTime.now())
			.institutionName(name)
			.preferredSchedule("화·목 오전 10시")
			.childAgeRange("만 4세, 만 5세")
			.childCount(20)
			.build());
	}

	private InstitutionConsultationRequest institutionRequest() {
		return new InstitutionConsultationRequest("몽키즈 어린이집", List.of(DayOfWeek.MONDAY), "오후 2시",
			List.of(ChildAgeGroup.AGE_4), 15, "010-1234-5678", null, true);
	}

	private String institutionBody() {
		return """
			{"institutionName": "몽키즈 어린이집", "preferredDays": ["MONDAY"], "preferredTime": "오후 2시",
			 "childAgeGroups": ["AGE_4"], "childCount": 15, "phone": "010-1234-5678", "privacyAgreed": true}
			""";
	}

	private Cookie tokenCookie(LoginMemberResponse member) {
		return new Cookie(TokenCookieManager.COOKIE_NAME, jwtProvider.createToken(member.id(), member.role().name()));
	}

	// 테스트 트랜잭션이 없어서 역할을 바꾼 뒤 직접 저장한다
	private LoginMemberResponse registerMember(String providerId, MemberRole role) {
		LoginMemberResponse member = memberService.loginOrRegister(
			new SocialLoginRequest(Provider.KAKAO, providerId, "회원" + providerId, null));
		Member saved = memberRepository.findById(member.id()).orElseThrow();
		ReflectionTestUtils.setField(saved, "role", role);
		memberRepository.save(saved);
		return member;
	}
}
