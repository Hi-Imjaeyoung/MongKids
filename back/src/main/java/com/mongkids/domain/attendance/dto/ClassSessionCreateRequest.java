package com.mongkids.domain.attendance.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 수업 반복 등록 요청 (EMP-FR-004). 기간 안의 고른 요일마다 날짜별 수업을 하나씩 만든다.
 */
public record ClassSessionCreateRequest(
	@NotNull Long institutionId,
	@NotNull Long staffId,
	@NotNull LocalDate firstDate,
	@NotNull LocalDate lastDate,
	@NotEmpty Set<DayOfWeek> daysOfWeek,
	@NotNull LocalTime startTime,
	@NotNull LocalTime endTime,
	@Size(max = 50) String target
) {
}
