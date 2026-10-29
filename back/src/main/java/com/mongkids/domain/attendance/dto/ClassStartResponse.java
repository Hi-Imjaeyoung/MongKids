package com.mongkids.domain.attendance.dto;

import java.time.LocalDateTime;

import com.mongkids.domain.attendance.entity.ClassStart;
import com.mongkids.domain.attendance.entity.ClassStartMethod;
import com.mongkids.domain.attendance.entity.ClassStartStatus;
import com.mongkids.domain.attendance.entity.GeoPoint;

/**
 * 수업 시작 기록. 좌표 원본은 화면에 보여 주지 않으므로 거리·정확도만 담는다.
 */
public record ClassStartResponse(
	ClassStartMethod method,
	ClassStartStatus status,
	LocalDateTime startedAt,
	Integer distanceMeters,
	Double accuracy,
	String reason,
	String rejectReason,
	String note,
	LocalDateTime processedAt
) {

	public static ClassStartResponse from(ClassStart start) {
		GeoPoint location = start.getLocation();
		return new ClassStartResponse(start.getMethod(), start.getStatus(), start.getStartedAt(),
			location == null ? null : location.getDistanceMeters(), location == null ? null : location.getAccuracy(),
			start.getReason(), start.getRejectReason(), start.getNote(), start.getProcessedAt());
	}
}
