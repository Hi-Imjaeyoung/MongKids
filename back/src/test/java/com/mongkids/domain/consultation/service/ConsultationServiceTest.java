package com.mongkids.domain.consultation.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.mongkids.domain.consultation.dto.ChildAgeGroup;
import com.mongkids.domain.consultation.dto.ConsultationStatusRequest;
import com.mongkids.domain.consultation.dto.InstitutionConsultationRequest;
import com.mongkids.domain.consultation.dto.PersonalConsultationRequest;
import com.mongkids.domain.consultation.entity.Consultation;
import com.mongkids.domain.consultation.entity.ConsultationStatus;
import com.mongkids.domain.consultation.entity.ConsultationType;
import com.mongkids.domain.consultation.entity.Gender;
import com.mongkids.domain.consultation.entity.InstitutionConsultation;
import com.mongkids.domain.consultation.entity.PersonalConsultation;
import com.mongkids.domain.consultation.repository.ConsultationRepository;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class ConsultationServiceTest {

	@Mock
	private ConsultationRepository consultationRepository;

	@Mock
	private MemberService memberService;

	@Mock
	private ApplicationEventPublisher eventPublisher;

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
		then(eventPublisher).should().publishEvent(any(NewConsultationCountChangedEvent.class));
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

	@Test
	@DisplayName("개인 상담은 빈 값을 null로, 전화번호는 숫자만 남기고 건강 메모가 있으면 건강정보 동의 시각을 저장한다")
	void createPersonalConsultation_savesValues() {
		// given
		given(consultationRepository.save(any(Consultation.class))).willAnswer(invocation -> invocation.getArgument(0));
		PersonalConsultationRequest request = personalRequest(LocalDate.of(2022, 3, 1), null, " 땅콩 알레르기 ", true);

		// when
		consultationService.createPersonalConsultation(request);

		// then
		ArgumentCaptor<Consultation> captor = ArgumentCaptor.forClass(Consultation.class);
		then(consultationRepository).should().save(captor.capture());
		PersonalConsultation saved = (PersonalConsultation)captor.getValue();
		assertThat(saved.getType()).isEqualTo(ConsultationType.PERSONAL);
		assertThat(saved.getGuardianName()).isEqualTo("김몽키");
		assertThat(saved.getPhone()).isEqualTo("01012345678");
		assertThat(saved.getChildBirthDate()).isEqualTo(LocalDate.of(2022, 3, 1));
		assertThat(saved.getChildAgeMonths()).isNull();
		assertThat(saved.isAttendsDaycare()).isTrue();
		assertThat(saved.getTemperament()).isNull();
		assertThat(saved.getHealthNote()).isEqualTo("땅콩 알레르기");
		assertThat(saved.getHealthAgreedAt()).isNotNull();
		assertThat(saved.getPrivacyAgreedAt()).isNotNull();
		then(eventPublisher).should().publishEvent(any(NewConsultationCountChangedEvent.class));
	}

	@Test
	@DisplayName("건강 메모가 없으면 건강정보 동의 시각을 남기지 않는다")
	void createPersonalConsultation_noHealthNote_noHealthAgreedAt() {
		// given
		given(consultationRepository.save(any(Consultation.class))).willAnswer(invocation -> invocation.getArgument(0));
		PersonalConsultationRequest request = personalRequest(null, 30, " ", true);

		// when
		consultationService.createPersonalConsultation(request);

		// then
		ArgumentCaptor<Consultation> captor = ArgumentCaptor.forClass(Consultation.class);
		then(consultationRepository).should().save(captor.capture());
		PersonalConsultation saved = (PersonalConsultation)captor.getValue();
		assertThat(saved.getChildAgeMonths()).isEqualTo(30);
		assertThat(saved.getHealthNote()).isNull();
		assertThat(saved.getHealthAgreedAt()).isNull();
	}

	@Test
	@DisplayName("생년월일과 개월 수를 둘 다 보내거나 둘 다 빠뜨리면 신청할 수 없다")
	void createPersonalConsultation_ageBothOrNone_invalidInput() {
		PersonalConsultationRequest both = personalRequest(LocalDate.of(2022, 3, 1), 30, null, false);
		PersonalConsultationRequest none = personalRequest(null, null, null, false);

		assertThatThrownBy(() -> consultationService.createPersonalConsultation(both))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.INVALID_INPUT);
		assertThatThrownBy(() -> consultationService.createPersonalConsultation(none))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.INVALID_INPUT);
		then(consultationRepository).should(never()).save(any());
	}

	@Test
	@DisplayName("건강 메모를 적고 건강정보 수집에 동의하지 않으면 신청할 수 없다")
	void createPersonalConsultation_healthNotAgreed_invalidInput() {
		// given
		PersonalConsultationRequest request = personalRequest(null, 30, "천식이 있어요", false);

		// when & then
		assertThatThrownBy(() -> consultationService.createPersonalConsultation(request))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.INVALID_INPUT);
		then(consultationRepository).should(never()).save(any());
	}

	@Test
	@DisplayName("처리 상태가 바뀔 때만 신규 건수 변경 이벤트를 발행한다 (FR-005)")
	void changeStatus_publishesOnlyWhenChanged() {
		// given
		InstitutionConsultation consultation = InstitutionConsultation.builder()
			.phone("0212345678")
			.privacyAgreedAt(LocalDateTime.now())
			.institutionName("햇살어린이집")
			.preferredSchedule("화·목 오전 10시")
			.childAgeRange("만 4세")
			.childCount(20)
			.build();
		given(consultationRepository.findById(1L)).willReturn(Optional.of(consultation));

		// when
		consultationService.changeStatus(9L, 1L, new ConsultationStatusRequest(ConsultationStatus.RECEIVED));

		// then
		then(eventPublisher).should(never()).publishEvent(any(NewConsultationCountChangedEvent.class));

		// when
		consultationService.changeStatus(9L, 1L, new ConsultationStatusRequest(ConsultationStatus.CONTACTED));

		// then
		then(eventPublisher).should().publishEvent(any(NewConsultationCountChangedEvent.class));
	}

	private PersonalConsultationRequest personalRequest(LocalDate birthDate, Integer ageMonths, String healthNote,
		boolean healthAgreed) {
		return new PersonalConsultationRequest(
			" 김몽키 ", "010-1234-5678", "김하늘", Gender.FEMALE, birthDate, ageMonths,
			" 서울시 마포구 몽키로 12 ", true, "  ", "공놀이", null, healthNote, "운동을 좋아해요",
			true, healthAgreed
		);
	}
}
