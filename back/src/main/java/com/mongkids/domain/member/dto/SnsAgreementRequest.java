package com.mongkids.domain.member.dto;

import jakarta.validation.constraints.NotNull;

/**
 * SNS 수신 동의 변경 요청 (FR-019). {@code true}면 동의, {@code false}면 철회다.
 */
public record SnsAgreementRequest(
	@NotNull Boolean agreed
) {
}
