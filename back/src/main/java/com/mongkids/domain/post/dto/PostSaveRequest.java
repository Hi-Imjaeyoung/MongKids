package com.mongkids.domain.post.dto;

import java.util.List;

import com.mongkids.domain.post.entity.PostCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 게시물 작성·수정 요청. {@code imageKeys}는 표시 순서대로 보내며 0번이 대표 이미지다.
 */
public record PostSaveRequest(
	@NotNull PostCategory category,
	@NotBlank @Size(max = 100) String title,
	@NotBlank String content,
	@NotNull List<@NotBlank String> imageKeys
) {
}
