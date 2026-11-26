package com.mongkids.domain.classplan.dto;

import com.mongkids.domain.classplan.entity.ClassPlanKind;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 계획안 등록. 연간·월간 모두 공개 블러본(fileKey)과 원본(originalFileKey)이 필요하다 (2026-11-26).
 */
public record ClassPlanCreateRequest(
	@NotNull ClassPlanKind kind,
	@NotBlank @Size(max = 50) String title,
	@NotBlank @Size(max = 100) String period,
	@NotBlank String fileKey,
	String originalFileKey
) {
}
