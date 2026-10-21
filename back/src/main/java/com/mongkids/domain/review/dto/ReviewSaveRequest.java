package com.mongkids.domain.review.dto;

import java.util.List;

import com.mongkids.domain.review.entity.ReviewType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 후기 작성·수정 요청. {@code imageKeys}는 업로드 URL로 올린 사진 키를 보여 줄 순서대로 담고, 첫 사진이 대표 사진이다.
 * 사진 개수·키 검증은 Service에서 한다 (FR-015).
 */
public record ReviewSaveRequest(
	@NotNull ReviewType type,
	@Min(1) @Max(5) int satisfaction,
	@NotBlank @Size(max = 1000) String content,
	List<@NotBlank String> imageKeys
) {

	public List<String> imageKeysOrEmpty() {
		return imageKeys == null ? List.of() : imageKeys;
	}
}
