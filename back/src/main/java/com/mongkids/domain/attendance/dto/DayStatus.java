package com.mongkids.domain.attendance.dto;

import com.mongkids.domain.attendance.entity.DayEnd;
import com.mongkids.domain.attendance.entity.DayStart;

/**
 * 하루 상태 (EMP-FR-020). 저장하지 않고 하루 시작·종료 기록에서 계산한다.
 */
public enum DayStatus {
	// 시작 전 (앞으로 날짜는 화면에서 "예정")
	NOT_STARTED,
	// 하루 시작 확정 (수업 중)
	STARTED,
	// 하루 시작 승인 대기
	PENDING,
	// 하루 시작 반려
	REJECTED,
	// 하루 종료 승인 대기
	END_PENDING,
	// 하루 종료 반려
	END_REJECTED,
	// 수업 종료
	ENDED;

	/**
	 * 종료 기록이 있어도 하루 시작이 반려되면 반려다. 시작 승인 대기 중에 종료가 확정되면 수업 종료다 (EMP-FR-010 규칙 유지).
	 */
	public static DayStatus of(DayStart start, DayEnd end) {
		if (start == null) {
			return NOT_STARTED;
		}
		if (start.isRejected()) {
			return REJECTED;
		}
		if (end == null) {
			return start.isPending() ? PENDING : STARTED;
		}
		if (end.isRejected()) {
			return END_REJECTED;
		}
		return end.isPending() ? END_PENDING : ENDED;
	}
}
