package com.mongkids.domain.review.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 후기 숨김·다시 공개 요청 (FR-021).
 */
public record ReviewHiddenRequest(
	@NotNull Boolean hidden
) {
}
