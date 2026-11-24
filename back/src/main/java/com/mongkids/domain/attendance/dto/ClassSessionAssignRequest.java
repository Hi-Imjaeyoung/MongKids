package com.mongkids.domain.attendance.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 직원 미정 수업 여러 회에 직원 한 명을 한꺼번에 배정 (EMP-FR-021).
 */
public record ClassSessionAssignRequest(
	@NotEmpty @Size(max = 200) List<@NotNull Long> classSessionIds,
	@NotNull Long staffId
) {
}
