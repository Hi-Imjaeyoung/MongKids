package com.mongkids.domain.attendance.dto;

import com.mongkids.domain.attendance.entity.Institution;

/**
 * 기관과 위치 (EMP-FR-012). 삭제하면 수업({@code classSessionCount})과 기록을 함께 지운다.
 */
public record InstitutionResponse(
	Long id,
	String name,
	String address,
	double latitude,
	double longitude,
	long classSessionCount
) {

	public static InstitutionResponse of(Institution institution, long classSessionCount) {
		return new InstitutionResponse(institution.getId(), institution.getName(), institution.getAddress(),
			institution.getLatitude(), institution.getLongitude(), classSessionCount);
	}
}
