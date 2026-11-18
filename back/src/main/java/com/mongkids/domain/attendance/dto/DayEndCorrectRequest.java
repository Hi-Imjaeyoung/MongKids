package com.mongkids.domain.attendance.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * 하루 사진·기관별 특이사항 정정 요청 (EMP-FR-020, EMP-FR-014 규칙 유지). 통째로 바꾼다.
 */
public record DayEndCorrectRequest(
	List<@NotBlank String> imageKeys,
	List<@Valid DayNoteRequest> notes
) {

	public List<String> imageKeysOrEmpty() {
		return imageKeys == null ? List.of() : imageKeys;
	}

	public List<DayNoteRequest> notesOrEmpty() {
		return notes == null ? List.of() : notes;
	}
}
