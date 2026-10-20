package com.mongkids.domain.post.dto;

import com.mongkids.domain.post.entity.LabCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 연구소 글 작성·수정 요청. 본문 형식은 {@link PostSaveRequest}와 같다.
 */
public record LabPostSaveRequest(
	@NotNull LabCategory category,
	@NotBlank @Size(max = 100) String title,
	@NotBlank String content
) {
}
