package com.mongkids.domain.attendance.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 정기 수업의 오늘 이후 미정 회차를 직원 한 명에게 배정 (EMP-FR-022).
 */
public record SeriesStaffAssignRequest(
	@NotNull Long staffId
) {
}
