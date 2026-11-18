package com.mongkids.domain.attendance.dto;

import java.time.LocalTime;

import com.mongkids.domain.attendance.entity.ClassSession;
import com.mongkids.domain.attendance.entity.Institution;

/**
 * 하루 기록에 딸린 수업 한 회. 수업마다 시작·종료를 기록하지 않으므로 상태는 없다 (EMP-FR-020).
 */
public record DayClassResponse(
	Long id,
	LocalTime startTime,
	LocalTime endTime,
	String target,
	Long institutionId,
	String institutionName,
	String institutionAddress
) {

	public static DayClassResponse from(ClassSession session) {
		Institution institution = session.getInstitution();
		return new DayClassResponse(session.getId(), session.getStartTime(), session.getEndTime(),
			session.getTarget(), institution.getId(), institution.getName(), institution.getAddress());
	}
}
