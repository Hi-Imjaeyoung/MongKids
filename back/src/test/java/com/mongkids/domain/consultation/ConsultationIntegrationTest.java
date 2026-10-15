package com.mongkids.domain.consultation;

import static org.assertj.core.api.Assertions.*;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.mongkids.domain.consultation.entity.InstitutionConsultation;
import com.mongkids.domain.consultation.repository.ConsultationRepository;
import com.mongkids.global.storage.FileStorage;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ConsultationIntegrationTest {

	private static final String URL = "/api/v1/institution-consultations";

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

	private String requestBody(boolean privacyAgreed) {
		return """
			{"institutionName": "몽키즈 어린이집", "preferredDays": ["FRIDAY", "MONDAY"], "preferredTime": "오후 2시",
			 "childAgeGroups": ["AGE_4"], "childCount": 15, "phone": "010-1234-5678",
			 "note": "강당 사용 가능", "privacyAgreed": %s}
			""".formatted(privacyAgreed);
	}
}
