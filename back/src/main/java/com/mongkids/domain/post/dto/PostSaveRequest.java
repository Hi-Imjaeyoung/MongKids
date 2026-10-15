package com.mongkids.domain.post.dto;

import com.mongkids.domain.post.entity.PostCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 게시물 작성·수정 요청. {@code content}는 편집기가 만든 본문 HTML이며 서버가 저장 전에 정화한다.
 * 사진은 본문의 {@code <figure class="post-photos"><img data-image-key>}로 들어오고, 처음 나온 사진이 대표 사진이다.
 */
public record PostSaveRequest(
	@NotNull PostCategory category,
	@NotBlank @Size(max = 100) String title,
	@NotBlank String content
) {
}
