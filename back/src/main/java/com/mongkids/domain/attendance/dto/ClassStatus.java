package com.mongkids.domain.attendance.dto;

import com.mongkids.domain.attendance.entity.ClassEnd;
import com.mongkids.domain.attendance.entity.ClassStart;

/**
 * 수업 상태 (EMP-FR-013). 저장하지 않고 수업 시작·종료 기록에서 계산한다.
 */
public enum ClassStatus {
	NOT_STARTED,
	STARTED,
	PENDING,
	REJECTED,
	ENDED;

	/**
	 * 수업 종료 기록이 있어도 수업 시작이 반려되면 반려다 (EMP-FR-010).
	 */
	public static ClassStatus of(ClassStart start, ClassEnd end) {
		if (start == null) {
			return NOT_STARTED;
		}
		if (start.isRejected()) {
			return REJECTED;
		}
		if (end != null) {
			return ENDED;
		}
		return start.isPending() ? PENDING : STARTED;
	}
}
