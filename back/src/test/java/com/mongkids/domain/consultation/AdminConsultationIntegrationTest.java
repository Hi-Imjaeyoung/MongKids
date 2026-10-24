package com.mongkids.domain.consultation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

import com.mongkids.domain.consultation.entity.Gender;
import com.mongkids.domain.consultation.entity.InstitutionConsultation;
import com.mongkids.domain.consultation.entity.PersonalConsultation;
import com.mongkids.domain.consultation.repository.ConsultationRepository;
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
class AdminConsultationIntegrationTest {

	private static final String URL = "/api/v1/admin/consultations";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private ConsultationRepository consultationRepository;

	@Autowired
	private JwtProvider jwtProvider;

	@MockitoBean
	private FileStorage fileStorage;

	@Test
	@DisplayName("관리자가 아니면 상담 신청을 볼 수 없다")
	void notAdmin_forbidden() throws Exception {
		mockMvc.perform(get(URL))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("A001"));

		mockMvc.perform(get(URL).cookie(tokenCookie(registerMember("member", MemberRole.MEMBER))))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.code").value("A002"));

		mockMvc.perform(get(URL).cookie(tokenCookie(registerMember("staff", MemberRole.STAFF))))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("관리자는 최근 접수 순 목록을 유형·상태로 거르고, 유형별 상세 항목을 본다")
	void listAndDetail() throws Exception {
		// given
		Cookie admin = tokenCookie(registerMember("admin", MemberRole.ADMIN));
		long institutionId = saveInstitution("햇살어린이집");
		long personalId = savePersonal("김보호");

		// when & then
		mockMvc.perform(get(URL).cookie(admin))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.totalElements").value(2))
			.andExpect(jsonPath("$.data.content[0].id").value(personalId))
			.andExpect(jsonPath("$.data.content[0].title").value("김보호"))
			.andExpect(jsonPath("$.data.content[0].status").value("RECEIVED"))
			.andExpect(jsonPath("$.data.content[1].title").value("햇살어린이집"));

		mockMvc.perform(get(URL).param("type", "INSTITUTION").cookie(admin))
			.andExpect(jsonPath("$.data.totalElements").value(1))
			.andExpect(jsonPath("$.data.content[0].id").value(institutionId));

		mockMvc.perform(get(URL).param("status", "COMPLETED").cookie(admin))
			.andExpect(jsonPath("$.data.totalElements").value(0));

		mockMvc.perform(get(URL + "/{id}", personalId).cookie(admin))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.type").value("PERSONAL"))
			.andExpect(jsonPath("$.data.personal.childName").value("아이"))
			.andExpect(jsonPath("$.data.personal.healthAgreedAt").exists())
			.andExpect(jsonPath("$.data.institution").doesNotExist());

		mockMvc.perform(get(URL + "/{id}", institutionId).cookie(admin))
			.andExpect(jsonPath("$.data.institution.institutionName").value("햇살어린이집"))
			.andExpect(jsonPath("$.data.institution.childCount").value(20))
			.andExpect(jsonPath("$.data.personal").doesNotExist());

		mockMvc.perform(get(URL + "/{id}", 9999L).cookie(admin))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.code").value("S001"));
	}

	@Test
	@DisplayName("상담 완료로 바꾸면 완료 시각이 남고, 다른 상태로 되돌리면 지워진다")
	void changeStatus() throws Exception {
		// given
		Cookie admin = tokenCookie(registerMember("admin", MemberRole.ADMIN));
		long id = saveInstitution("햇살어린이집");

		// when & then
		mockMvc.perform(put(URL + "/{id}/status", id).cookie(admin)
				.contentType(MediaType.APPLICATION_JSON).content("""
					{"status": "COMPLETED"}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.status").value("COMPLETED"))
			.andExpect(jsonPath("$.data.completedAt").exists());

		mockMvc.perform(get(URL).param("status", "COMPLETED").cookie(admin))
			.andExpect(jsonPath("$.data.totalElements").value(1));

		mockMvc.perform(put(URL + "/{id}/status", id).cookie(admin)
				.contentType(MediaType.APPLICATION_JSON).content("""
					{"status": "CONTACTED"}
					"""))
			.andExpect(jsonPath("$.data.status").value("CONTACTED"))
			.andExpect(jsonPath("$.data.completedAt").doesNotExist());

		mockMvc.perform(put(URL + "/{id}/status", id).cookie(admin)
				.contentType(MediaType.APPLICATION_JSON).content("{}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.fieldErrors[0].field").value("status"));
	}

	private long saveInstitution(String name) {
		return consultationRepository.save(InstitutionConsultation.builder()
			.phone("0212345678")
			.privacyAgreedAt(LocalDateTime.now())
			.institutionName(name)
			.preferredSchedule("화·목 오전 10시")
			.childAgeRange("만 4세, 만 5세")
			.childCount(20)
			.build()).getId();
	}

	private long savePersonal(String guardianName) {
		LocalDateTime now = LocalDateTime.now();
		return consultationRepository.save(PersonalConsultation.builder()
			.phone("01012345678")
			.privacyAgreedAt(now)
			.guardianName(guardianName)
			.childName("아이")
			.childGender(Gender.MALE)
			.childBirthDate(LocalDate.of(2021, 5, 3))
			.address("서울시")
			.attendsDaycare(true)
			.healthNote("땅콩 알레르기")
			.healthAgreedAt(now)
			.build()).getId();
	}

	private Cookie tokenCookie(LoginMemberResponse member) {
		return new Cookie(TokenCookieManager.COOKIE_NAME, jwtProvider.createToken(member.id(), member.role().name()));
	}

	private LoginMemberResponse registerMember(String providerId, MemberRole role) {
		LoginMemberResponse member = memberService.loginOrRegister(
			new SocialLoginRequest(Provider.KAKAO, providerId, "회원" + providerId, null));
		ReflectionTestUtils.setField(memberRepository.findById(member.id()).orElseThrow(), "role", role);
		return member;
	}
}
