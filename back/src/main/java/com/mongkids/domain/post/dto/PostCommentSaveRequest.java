package com.mongkids.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 댓글 작성 요청. 본문은 일반 텍스트다.
 */
public record PostCommentSaveRequest(
	@NotBlank @Size(max = 1000) String content
) {
}
