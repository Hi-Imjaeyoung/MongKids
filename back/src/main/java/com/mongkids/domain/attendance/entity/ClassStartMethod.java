package com.mongkids.domain.attendance.entity;

/**
 * 수업 시작 기록을 남긴 방식 (EMP-FR-001, EMP-FR-011).
 */
public enum ClassStartMethod {
	// 위치로 반경 안을 확인해 자동 확정
	LOCATION,
	// 직원이 사유를 적어 수동 등록, 관리자 승인 필요
	MANUAL,
	// 관리자가 직접 등록
	ADMIN
}
