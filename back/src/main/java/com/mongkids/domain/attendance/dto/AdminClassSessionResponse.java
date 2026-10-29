package com.mongkids.domain.attendance.dto;

/**
 * 관리자 기록 조회 한 줄 (EMP-FR-003, EMP-FR-011). 수업 정보에 담당 직원을 더한다.
 */
public record AdminClassSessionResponse(
	Long staffId,
	String staffName,
	ClassSessionResponse classSession
) {
}
