package com.mongkids.domain.attendance.dto;

/**
 * 위치 기반 하루 시작·종료 확인 결과 (EMP-FR-020). 반경 밖이면 {@code confirmed}가 false이고 기록은 저장하지 않는다.
 * 화면은 확인 기관·거리·기준 반경으로 안내한다.
 */
public record DayCheckResultResponse(
	boolean confirmed,
	int distanceMeters,
	int radiusMeters,
	String institutionName,
	WorkDayResponse day
) {
}
