package com.mongkids.domain.attendance.entity;

/**
 * 하루 기록 가운데 어느 쪽을 확인했는지 (EMP-FR-020).
 */
public enum DayCheckType {
	// 하루 시작: 그날 첫 수업 기관
	START,
	// 하루 종료: 그날 마지막 수업 기관
	END
}
