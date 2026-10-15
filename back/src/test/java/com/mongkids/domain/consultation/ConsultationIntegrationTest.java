package com.mongkids.domain.consultation;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.mongkids.domain.consultation.entity.Gender;
import com.mongkids.domain.consultation.entity.InstitutionConsultation;
import com.mongkids.domain.consultation.entity.PersonalConsultation;
import com.mongkids.domain.consultation.repository.ConsultationRepository;
import com.mongkids.global.storage.FileStorage;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ConsultationIntegrationTest {

	private static final String URL = "/api/v1/institution-consultations";
	private static final String PERSONAL_URL = "/api/v1/personal-consultations";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ConsultationRepository consultationRepository;

	@MockitoBean
	private FileStorage fileStorage;

	@Test
	@DisplayName("로그인하지 않아도 기관 상담을 신청할 수 있다")
	void create_withoutLogin_created() throws Exception {
		// when
		String body = mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(requestBody(true)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.success").value(true))
			.andReturn().getResponse().getContentAsString();

		// then
		long id = objectMapper.readTree(body).path("data").path("id").asLong();
		InstitutionConsultation saved = (InstitutionConsultation)consultationRepository.findById(id).orElseThrow();
		assertThat(saved.getPreferredSchedule()).isEqualTo("월·금 오후 2시");
		assertThat(saved.getChildAgeRange()).isEqualTo("만 4세");
		assertThat(saved.getPhone()).isEqualTo("01012345678");
	}

	@Test
	@DisplayName("개인정보 수집에 동의하지 않으면 400을 응답한다")
	void create_privacyNotAgreed_badRequest() throws Exception {
		mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(requestBody(false)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("C001"))
			.andExpect(jsonPath("$.error.fieldErrors[0].field").value("privacyAgreed"));
		assertThat(consultationRepository.count()).isZero();
	}

	@Test
	@DisplayName("필수값이 없거나 전화번호 형식이 틀리면 400과 fieldErrors를 응답한다")
	void create_invalidInput_badRequest() throws Exception {
		mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"institutionName": "", "preferredDays": [], "preferredTime": "오전",
					 "childAgeGroups": ["AGE_4"], "childCount": 10, "phone": "1234", "privacyAgreed": true}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("C001"))
			.andExpect(jsonPath("$.error.fieldErrors.length()").value(3));
	}

	@Test
	@DisplayName("로그인하지 않아도 개인 상담을 신청할 수 있다")
	void createPersonal_withoutLogin_created() throws Exception {
		// when
		String body = mockMvc.perform(post(PERSONAL_URL).contentType(MediaType.APPLICATION_JSON)
				.content(personalRequestBody("01012345678", true)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.success").value(true))
			.andReturn().getResponse().getContentAsString();

		// then
		long id = objectMapper.readTree(body).path("data").path("id").asLong();
		PersonalConsultation saved = (PersonalConsultation)consultationRepository.findById(id).orElseThrow();
		assertThat(saved.getChildGender()).isEqualTo(Gender.MALE);
		assertThat(saved.getChildBirthDate()).isEqualTo(LocalDate.of(2021, 5, 10));
		assertThat(saved.getHealthAgreedAt()).isNotNull();
		assertThat(saved.getPhone()).isEqualTo("01012345678");
	}

	@Test
	@DisplayName("개인 상담에서 개인정보 수집에 동의하지 않으면 400을 응답한다")
	void createPersonal_privacyNotAgreed_badRequest() throws Exception {
		mockMvc.perform(post(PERSONAL_URL).contentType(MediaType.APPLICATION_JSON)
				.content(personalRequestBody("01012345678", false)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("C001"))
			.andExpect(jsonPath("$.error.fieldErrors[0].field").value("privacyAgreed"));
		assertThat(consultationRepository.count()).isZero();
	}

	@Test
	@DisplayName("개인 상담은 010 휴대전화 번호만 받는다")
	void createPersonal_landline_badRequest() throws Exception {
		mockMvc.perform(post(PERSONAL_URL).contentType(MediaType.APPLICATION_JSON)
				.content(personalRequestBody("02-123-4567", true)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("C001"))
			.andExpect(jsonPath("$.error.fieldErrors[0].field").value("phone"));
	}

	@Test
	@DisplayName("개인 상담에서 건강 메모를 적고 건강정보 동의를 하지 않으면 400을 응답한다")
	void createPersonal_healthNotAgreed_badRequest() throws Exception {
		mockMvc.perform(post(PERSONAL_URL).contentType(MediaType.APPLICATION_JSON)
				.content(personalRequestBody("01012345678", true).replace("\"healthAgreed\": true", "\"healthAgreed\": false")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("C001"));
		assertThat(consultationRepository.count()).isZero();
	}

	@Test
	@DisplayName("개인 상담 필수값이 없으면 400과 fieldErrors를 응답한다")
	void createPersonal_missingRequired_badRequest() throws Exception {
		mockMvc.perform(post(PERSONAL_URL).contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"guardianName": "", "phone": "01012345678", "childName": "김하늘", "childAgeMonths": 30,
					 "address": "", "privacyAgreed": true}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("C001"))
			.andExpect(jsonPath("$.error.fieldErrors.length()").value(4));
	}

	private String personalRequestBody(String phone, boolean privacyAgreed) {
		return """
			{"guardianName": "김몽키", "phone": "%s", "childName": "김하늘", "childGender": "MALE",
			 "childBirthDate": "2021-05-10", "address": "서울시 마포구 몽키로 12", "attendsDaycare": false,
			 "favoritePlay": "공놀이", "healthNote": "땅콩 알레르기", "privacyAgreed": %s, "healthAgreed": true}
			""".formatted(phone, privacyAgreed);
	}

	private String requestBody(boolean privacyAgreed) {
		return """
			{"institutionName": "몽키즈 어린이집", "preferredDays": ["FRIDAY", "MONDAY"], "preferredTime": "오후 2시",
			 "childAgeGroups": ["AGE_4"], "childCount": 15, "phone": "010-1234-5678",
			 "note": "강당 사용 가능", "privacyAgreed": %s}
			""".formatted(privacyAgreed);
	}
}
