package com.mongkids.domain.attendance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 기관별 특이사항 (EMP-FR-020). 비어 있으면 저장하지 않는다. 그날 수업한 기관인지는 Service에서 확인한다.
 */
public record DayNoteRequest(
	@NotNull Long institutionId,
	@Size(max = 2000) String note
) {
}
