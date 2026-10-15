package com.mongkids.domain.consultation.dto;

import java.time.DayOfWeek;
import java.util.List;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 기관 상담 신청 요청 (FR-001). 비회원도 보낼 수 있고(FR-003), 개인정보 수집 동의가 필수다(FR-023).
 * 희망 요일·원아 연령은 선택지로 받아 서버가 문자열로 합쳐 저장한다.
 */
public record InstitutionConsultationRequest(
	@NotBlank @Size(max = 100) String institutionName,
	@NotEmpty List<@NotNull DayOfWeek> preferredDays,
	@NotBlank @Size(max = 100) String preferredTime,
	@NotEmpty List<@NotNull ChildAgeGroup> childAgeGroups,
	@NotNull @Min(1) @Max(999) Integer childCount,
	// 휴대전화와 어린이집 유선번호(02-123-4567 등)를 모두 받는다
	@NotBlank @Pattern(regexp = "^0\\d{1,2}-?\\d{3,4}-?\\d{4}$") String phone,
	@Size(max = 1000) String note,
	@AssertTrue boolean privacyAgreed
) {
}
