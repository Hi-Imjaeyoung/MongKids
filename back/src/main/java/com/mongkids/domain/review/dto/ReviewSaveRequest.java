package com.mongkids.domain.review.dto;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 후기 작성·수정 요청. {@code imageKeys}는 업로드 URL로 올린 사진 키를 보여 줄 순서대로 담고, 첫 사진이 대표 사진이다.
 * 사진 개수·키 검증은 Service에서 한다 (FR-015). 후기 유형은 받지 않는다 (2026-11-05, 모두 수업 후기).
 */
public record ReviewSaveRequest(
	@Min(1) @Max(5) int satisfaction,
	@NotBlank @Size(max = 1000) String content,
	List<@NotBlank String> imageKeys
) {

	public List<String> imageKeysOrEmpty() {
		return imageKeys == null ? List.of() : imageKeys;
	}
}
