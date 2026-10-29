package com.mongkids.domain.attendance.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 수업 종료 제출·정정 요청 (EMP-FR-010, EMP-FR-014). {@code imageKeys}는 보여 줄 순서대로 담는다.
 * 사진 개수(1~20장)·키 검증은 Service에서 한다.
 */
public record ClassEndSaveRequest(
	@Size(max = 2000) String note,
	List<@NotBlank String> imageKeys
) {

	public List<String> imageKeysOrEmpty() {
		return imageKeys == null ? List.of() : imageKeys;
	}
}
