package com.mongkids.domain.attendance.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.mongkids.domain.attendance.entity.ClassType;

/**
 * 수업 반복 등록 요청 (EMP-FR-004). 기간 안의 고른 요일마다 날짜별 수업을 하나씩 만든다.
 */
public record ClassSessionCreateRequest(
	@NotNull Long institutionId,
	// null이면 직원 미정 (EMP-FR-021)
	Long staffId,
	@NotNull LocalDate firstDate,
	@NotNull LocalDate lastDate,
	@NotEmpty Set<DayOfWeek> daysOfWeek,
	@NotNull LocalTime startTime,
	@NotNull LocalTime endTime,
	@Size(max = 50) String target,
	// null이면 정기 수업
	ClassType classType
) {
}
