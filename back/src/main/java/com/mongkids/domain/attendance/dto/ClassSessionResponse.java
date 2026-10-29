package com.mongkids.domain.attendance.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.mongkids.domain.attendance.entity.ClassSession;
import com.mongkids.domain.attendance.entity.Institution;

/**
 * 수업 한 회와 상태 (EMP-FR-013). 시작·종료 기록이 없으면 각각 null이다.
 */
public record ClassSessionResponse(
	Long id,
	LocalDate classDate,
	LocalTime startTime,
	LocalTime endTime,
	String target,
	String institutionName,
	String institutionAddress,
	ClassStatus status,
	ClassStartResponse start,
	ClassEndResponse end
) {

	public static ClassSessionResponse of(ClassSession session, ClassStatus status, ClassStartResponse start,
		ClassEndResponse end) {
		Institution institution = session.getInstitution();
		return new ClassSessionResponse(session.getId(), session.getClassDate(), session.getStartTime(),
			session.getEndTime(), session.getTarget(), institution.getName(), institution.getAddress(), status, start,
			end);
	}
}
