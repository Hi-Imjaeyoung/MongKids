package com.mongkids.domain.attendance.dto;

/**
 * 기관별 특이사항 (EMP-FR-020). 그날 수업한 기관마다 하나씩, 쓰지 않았으면 {@code note}가 null이다.
 */
public record DayNoteResponse(
	Long institutionId,
	String institutionName,
	String note
) {
}
