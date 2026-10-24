package com.mongkids.domain.consultation.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.mongkids.domain.consultation.dto.ChildAgeGroup;
import com.mongkids.domain.consultation.dto.ConsultationCreateResponse;
import com.mongkids.domain.consultation.dto.ConsultationDetailResponse;
import com.mongkids.domain.consultation.dto.ConsultationStatusRequest;
import com.mongkids.domain.consultation.dto.ConsultationSummaryResponse;
import com.mongkids.domain.consultation.dto.InstitutionConsultationRequest;
import com.mongkids.domain.consultation.dto.PersonalConsultationRequest;
import com.mongkids.domain.consultation.entity.Consultation;
import com.mongkids.domain.consultation.entity.ConsultationStatus;
import com.mongkids.domain.consultation.entity.ConsultationType;
import com.mongkids.domain.consultation.entity.InstitutionConsultation;
import com.mongkids.domain.consultation.entity.PersonalConsultation;
import com.mongkids.domain.consultation.repository.ConsultationRepository;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.global.common.PageResponse;
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

	static final int MAX_PAGE_SIZE = 100;
	static final Sort LATEST = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

	private final ConsultationRepository consultationRepository;
	private final MemberService memberService;

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

	/**
	 * 개인 상담 신청을 저장한다 (FR-002). 인증 없이 호출된다 (FR-003).
	 * 건강 메모가 있을 때만 건강정보 별도 동의를 확인하고 동의 시각을 남긴다 (FR-023).
	 */
	@Transactional
	public ConsultationCreateResponse createPersonalConsultation(PersonalConsultationRequest request) {
		// 생년월일과 개월 수 중 정확히 하나만 받는다
		if ((request.childBirthDate() == null) == (request.childAgeMonths() == null)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		String healthNote = stripToNull(request.healthNote());
		if (healthNote != null && !Boolean.TRUE.equals(request.healthAgreed())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}

		LocalDateTime now = LocalDateTime.now();
		PersonalConsultation consultation = PersonalConsultation.builder()
			.guardianName(request.guardianName().strip())
			.phone(normalizePhone(request.phone()))
			.childName(request.childName().strip())
			.childGender(request.childGender())
			.childBirthDate(request.childBirthDate())
			.childAgeMonths(request.childAgeMonths())
			.address(request.address().strip())
			.attendsDaycare(request.attendsDaycare())
			.temperament(stripToNull(request.temperament()))
			.favoritePlay(stripToNull(request.favoritePlay()))
			.favoriteCharacter(stripToNull(request.favoriteCharacter()))
			.healthNote(healthNote)
			.healthAgreedAt(healthNote != null ? now : null)
			.motivation(stripToNull(request.motivation()))
			.privacyAgreedAt(now)
			.build();
		return new ConsultationCreateResponse(consultationRepository.save(consultation).getId());
	}

	/**
	 * 관리자 상담 신청 목록 (FR-004). 최근 접수 순이고 유형·처리 상태로 거를 수 있다.
	 */
	public PageResponse<ConsultationSummaryResponse> getConsultations(Long adminId, ConsultationType type,
		ConsultationStatus status, Pageable pageable) {
		memberService.validateAdmin(adminId);
		Pageable latest = PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), MAX_PAGE_SIZE),
			LATEST);
		return PageResponse.from(consultationRepository.search(type, status, latest),
			ConsultationSummaryResponse::from);
	}

	public ConsultationDetailResponse getConsultation(Long adminId, Long consultationId) {
		memberService.validateAdmin(adminId);
		return ConsultationDetailResponse.from(getConsultation(consultationId));
	}

	/**
	 * 처리 상태를 바꾼다 (FR-026). 상담 완료 시각은 보관 기간(FR-023)의 기준이 된다.
	 */
	@Transactional
	public ConsultationDetailResponse changeStatus(Long adminId, Long consultationId,
		ConsultationStatusRequest request) {
		memberService.validateAdmin(adminId);
		Consultation consultation = getConsultation(consultationId);
		consultation.changeStatus(request.status());
		return ConsultationDetailResponse.from(consultation);
	}

	private Consultation getConsultation(Long consultationId) {
		return consultationRepository.findById(consultationId)
			.orElseThrow(() -> new BusinessException(ErrorCode.CONSULTATION_NOT_FOUND));
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

	private String stripToNull(String value) {
		return StringUtils.hasText(value) ? value.strip() : null;
	}
}
