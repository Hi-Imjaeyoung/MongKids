package com.mongkids.domain.attendance.dto;

import java.time.LocalDateTime;

import com.mongkids.domain.attendance.entity.ClassStartMethod;
import com.mongkids.domain.attendance.entity.ClassStartStatus;
import com.mongkids.domain.attendance.entity.DayCheck;
import com.mongkids.domain.attendance.entity.GeoPoint;

/**
 * 하루 시작·종료 기록 (EMP-FR-020). 좌표 원본은 화면에 보여 주지 않으므로 확인 기관·거리·정확도만 담는다.
 */
public record DayCheckResponse(
	ClassStartMethod method,
	ClassStartStatus status,
	LocalDateTime checkedAt,
	Long institutionId,
	String institutionName,
	Integer distanceMeters,
	Double accuracy,
	String reason,
	String rejectReason,
	LocalDateTime processedAt
) {

	public static DayCheckResponse from(DayCheck check) {
		if (check == null) {
			return null;
		}
		GeoPoint location = check.getLocation();
		return new DayCheckResponse(check.getMethod(), check.getStatus(), check.getCheckedAt(),
			check.getInstitution().getId(), check.getInstitution().getName(),
			location == null ? null : location.getDistanceMeters(), location == null ? null : location.getAccuracy(),
			check.getReason(), check.getRejectReason(), check.getProcessedAt());
	}
}
