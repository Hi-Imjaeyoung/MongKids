package com.mongkids.domain.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 관리자 답글 저장 요청 (FR-017). 후기당 답글은 하나이고, 다시 저장하면 기존 답글을 고친다.
 */
public record ReviewReplyRequest(
	@NotBlank @Size(max = 1000) String content
) {
}
