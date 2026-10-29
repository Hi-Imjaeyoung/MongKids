package com.mongkids.domain.attendance.entity;

/**
 * 수업 시작 기록의 승인 상태 (EMP-FR-011).
 */
public enum ClassStartStatus {
	// 자동 확정·관리자 승인·관리자 직접 등록
	CONFIRMED,
	// 수동 등록 후 관리자 승인 대기
	PENDING,
	// 관리자가 반려. 직원이 다시 확인하면 기록을 덮어쓴다
	REJECTED
}
