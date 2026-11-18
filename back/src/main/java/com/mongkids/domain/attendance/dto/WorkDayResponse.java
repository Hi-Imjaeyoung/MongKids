package com.mongkids.domain.attendance.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 직원 한 명의 하루 (EMP-FR-020). 그날 수업(시간순)과 하루 시작·종료 기록, 하루 사진·기관별 특이사항을 담는다.
 * 기록이 없으면 {@code start}·{@code end}는 null, 사진·특이사항은 하루 종료를 제출한 뒤에만 채운다.
 * {@code correctable}은 작성자가 지금 사진·특이사항을 정정할 수 있는지다 (EMP-FR-014 규칙 유지).
 * 하루 단위 적용 전 날짜는 {@code legacyClasses}에 수업 단위 기록을 읽기 전용으로 담는다.
 */
public record WorkDayResponse(
	LocalDate date,
	DayStatus status,
	List<DayClassResponse> classes,
	DayCheckResponse start,
	DayCheckResponse end,
	List<ClassEndResponse.ImageResponse> images,
	List<DayNoteResponse> notes,
	LocalDateTime correctedAt,
	boolean correctable,
	List<ClassSessionResponse> legacyClasses
) {
}
