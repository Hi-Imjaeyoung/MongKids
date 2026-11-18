package com.mongkids.domain.attendance.dto;

/**
 * 관리자 화면의 직원 하루 (EMP-SCR-006, EMP-FR-020).
 */
public record AdminWorkDayResponse(
	Long staffId,
	String staffName,
	WorkDayResponse day
) {
}
