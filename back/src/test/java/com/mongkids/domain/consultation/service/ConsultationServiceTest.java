package com.mongkids.domain.consultation.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.DayOfWeek;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mongkids.domain.consultation.dto.ChildAgeGroup;
import com.mongkids.domain.consultation.dto.InstitutionConsultationRequest;
import com.mongkids.domain.consultation.entity.Consultation;
import com.mongkids.domain.consultation.entity.ConsultationStatus;
import com.mongkids.domain.consultation.entity.ConsultationType;
import com.mongkids.domain.consultation.entity.InstitutionConsultation;
import com.mongkids.domain.consultation.repository.ConsultationRepository;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class ConsultationServiceTest {

	@Mock
	private ConsultationRepository consultationRepository;

	@InjectMocks
	private ConsultationService consultationService;

	@Test
	@DisplayName("요일·연령은 순서대로 합치고 전화번호는 숫자만 남겨 저장한다")
	void createInstitutionConsultation_savesJoinedValues() {
		// given
		given(consultationRepository.save(any(Consultation.class))).willAnswer(invocation -> invocation.getArgument(0));
		InstitutionConsultationRequest request = new InstitutionConsultationRequest(
			" 몽키즈 어린이집 ",
			List.of(DayOfWeek.WEDNESDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
			" 오전 10시~11시 ",
			List.of(ChildAgeGroup.AGE_5, ChildAgeGroup.AGE_3),
			24,
			"02-123-4567",
			"  ",
			true
		);

		// when
		consultationService.createInstitutionConsultation(request);

		// then
		ArgumentCaptor<Consultation> captor = ArgumentCaptor.forClass(Consultation.class);
		then(consultationRepository).should().save(captor.capture());
		InstitutionConsultation saved = (InstitutionConsultation)captor.getValue();
		assertThat(saved.getType()).isEqualTo(ConsultationType.INSTITUTION);
		assertThat(saved.getStatus()).isEqualTo(ConsultationStatus.RECEIVED);
		assertThat(saved.getInstitutionName()).isEqualTo("몽키즈 어린이집");
		assertThat(saved.getPreferredSchedule()).isEqualTo("월·수 오전 10시~11시");
		assertThat(saved.getChildAgeRange()).isEqualTo("만 3세, 만 5세");
		assertThat(saved.getChildCount()).isEqualTo(24);
		assertThat(saved.getPhone()).isEqualTo("021234567");
		assertThat(saved.getNote()).isNull();
		assertThat(saved.getPrivacyAgreedAt()).isNotNull();
	}

	@Test
	@DisplayName("일요일 수업은 신청할 수 없다")
	void createInstitutionConsultation_sunday_invalidInput() {
		// given
		InstitutionConsultationRequest request = new InstitutionConsultationRequest(
			"몽키즈 어린이집", List.of(DayOfWeek.SUNDAY), "오전", List.of(ChildAgeGroup.AGE_4), 10,
			"010-1234-5678", null, true
		);

		// when & then
		assertThatThrownBy(() -> consultationService.createInstitutionConsultation(request))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.INVALID_INPUT);
		then(consultationRepository).should(never()).save(any());
	}
}
