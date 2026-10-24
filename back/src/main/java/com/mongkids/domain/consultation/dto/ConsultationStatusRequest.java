package com.mongkids.domain.consultation.dto;

import com.mongkids.domain.consultation.entity.ConsultationStatus;

import jakarta.validation.constraints.NotNull;

/**
 * 상담 처리 상태 변경 요청 (FR-026).
 */
public record ConsultationStatusRequest(
	@NotNull ConsultationStatus status
) {
}
