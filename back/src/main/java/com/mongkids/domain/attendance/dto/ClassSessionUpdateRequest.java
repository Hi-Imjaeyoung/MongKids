package com.mongkids.domain.attendance.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.mongkids.domain.attendance.entity.ClassType;

/**
 * 수업 한 회 수정 요청 (EMP-FR-004).
 */
public record ClassSessionUpdateRequest(
	@NotNull Long institutionId,
	// null이면 직원 미정 (EMP-FR-021)
	Long staffId,
	@NotNull LocalDate classDate,
	@NotNull LocalTime startTime,
	@NotNull LocalTime endTime,
	@Size(max = 50) String target,
	// null이면 정기 수업
	ClassType classType
) {
}
