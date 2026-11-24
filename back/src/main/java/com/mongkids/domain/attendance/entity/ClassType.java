package com.mongkids.domain.attendance.entity;

/**
 * 수업 유형 (EMP-FR-004). 행사·특별활동도 정기 수업과 같은 할당·하루 시작·종료 흐름을 쓴다.
 * 행사 장소는 기관과 같이 등록한다 (사용자 결정 2026-11-24).
 */
public enum ClassType {
	// 정기 수업
	REGULAR,
	// 행사·특별활동
	EVENT
}
