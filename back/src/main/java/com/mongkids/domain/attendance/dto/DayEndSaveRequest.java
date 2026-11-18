package com.mongkids.domain.attendance.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * 하루 종료 제출 요청 (EMP-FR-020). 사유가 없으면 위치로 판정하고(위치 필수), 사유가 있으면 수동 등록이다(위치 선택).
 * {@code imageKeys}는 보여 줄 순서대로 담는다. 사진 개수(1~20장)·키 검증은 Service에서 한다.
 */
public record DayEndSaveRequest(
	@Size(max = 200) String reason,
	@DecimalMin("-90") @DecimalMax("90") Double latitude,
	@DecimalMin("-180") @DecimalMax("180") Double longitude,
	@PositiveOrZero Double accuracy,
	List<@NotBlank String> imageKeys,
	List<@Valid DayNoteRequest> notes
) {

	public boolean hasLocation() {
		return latitude != null && longitude != null;
	}

	public boolean isManual() {
		return reason != null && !reason.isBlank();
	}

	public List<String> imageKeysOrEmpty() {
		return imageKeys == null ? List.of() : imageKeys;
	}

	public List<DayNoteRequest> notesOrEmpty() {
		return notes == null ? List.of() : notes;
	}
}
