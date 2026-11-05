package com.mongkids.domain.consultation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.mongkids.domain.consultation.entity.ParticipantGroup;
import com.mongkids.domain.consultation.entity.QuoteProgram;
import com.mongkids.domain.consultation.entity.VenueType;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 운동회 견적 신청 요청 (FR-031). 비회원도 보낼 수 있고(FR-003), 개인정보 수집 동의가 필수다(FR-023).
 * 항목 사이의 조건(일정 미정이 아니면 날짜·시간, 실내·실외면 주소, 인원 하나 이상)은 Service에서 검사한다.
 */
public record QuoteConsultationRequest(
	@NotBlank @Size(max = 100) String institutionName,
	boolean scheduleUndecided,
	LocalDate eventDate,
	LocalTime eventTime,
	@NotNull VenueType venueType,
	@Size(max = 255) String address,
	@NotEmpty List<@NotNull ParticipantGroup> participantGroups,
	@Min(1) @Max(9999) Integer childCount,
	@Min(1) @Max(9999) Integer guardianCount,
	List<@NotNull QuoteProgram> programs,
	// 기관·단체의 유선번호도 받는다 (FR-001과 같은 형식)
	@NotBlank @Pattern(regexp = "^0\\d{1,2}-?\\d{3,4}-?\\d{4}$") String phone,
	@Size(max = 1000) String note,
	@AssertTrue boolean privacyAgreed
) {
}
