package com.mongkids.domain.attendance.dto;

/**
 * 위치 기반 수업 시작 확인 결과 (EMP-FR-001). 반경 밖이면 {@code confirmed}가 false이고 기록은 저장하지 않는다.
 * 화면은 거리와 기준 반경으로 안내한다.
 */
public record ClassStartResultResponse(
	boolean confirmed,
	int distanceMeters,
	int radiusMeters,
	ClassSessionResponse classSession
) {
}
