package com.mongkids.domain.consultation.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.mongkids.domain.consultation.dto.ChildAgeGroup;
import com.mongkids.domain.consultation.dto.ConsultationCreateResponse;
import com.mongkids.domain.consultation.dto.InstitutionConsultationRequest;
import com.mongkids.domain.consultation.entity.InstitutionConsultation;
import com.mongkids.domain.consultation.repository.ConsultationRepository;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ConsultationService {

	// 수업은 월~토만 받는다
	private static final Map<DayOfWeek, String> DAY_LABELS = Map.of(
		DayOfWeek.MONDAY, "월",
		DayOfWeek.TUESDAY, "화",
		DayOfWeek.WEDNESDAY, "수",
		DayOfWeek.THURSDAY, "목",
		DayOfWeek.FRIDAY, "금",
		DayOfWeek.SATURDAY, "토"
	);

	private final ConsultationRepository consultationRepository;

	/**
	 * 기관 상담 신청을 저장한다 (FR-001). 인증 없이 호출된다 (FR-003).
	 * 희망 일정은 {@code "월·수 오전 10시"}, 원아 연령은 {@code "만 3세, 만 4세"} 형태로 합쳐 저장한다.
	 */
	@Transactional
	public ConsultationCreateResponse createInstitutionConsultation(InstitutionConsultationRequest request) {
		InstitutionConsultation consultation = InstitutionConsultation.builder()
			.institutionName(request.institutionName().strip())
			.preferredSchedule(toSchedule(request))
			.childAgeRange(toAgeRange(request))
			.childCount(request.childCount())
			.phone(normalizePhone(request.phone()))
			.note(StringUtils.hasText(request.note()) ? request.note().strip() : null)
			.privacyAgreedAt(LocalDateTime.now())
			.build();
		return new ConsultationCreateResponse(consultationRepository.save(consultation).getId());
	}

	private String toSchedule(InstitutionConsultationRequest request) {
		if (request.preferredDays().stream().anyMatch(day -> !DAY_LABELS.containsKey(day))) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		String days = request.preferredDays().stream()
			.distinct()
			.sorted()
			.map(DAY_LABELS::get)
			.collect(Collectors.joining("·"));
		return days + " " + request.preferredTime().strip();
	}

	private String toAgeRange(InstitutionConsultationRequest request) {
		return request.childAgeGroups().stream()
			.distinct()
			.sorted()
			.map(ChildAgeGroup::getLabel)
			.collect(Collectors.joining(", "));
	}

	private String normalizePhone(String phone) {
		return phone.replaceAll("\\D", "");
	}
}
