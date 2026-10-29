package com.mongkids.domain.attendance.dto;

import jakarta.validation.constraints.Size;

/**
 * 수업 시작 특이사항 저장 요청 (EMP-FR-002). 비우면 특이사항을 지운다.
 */
public record ClassStartNoteRequest(
	@Size(max = 1000) String note
) {
}
