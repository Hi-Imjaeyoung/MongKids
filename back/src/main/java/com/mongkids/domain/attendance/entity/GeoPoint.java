package com.mongkids.domain.attendance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 기기가 보낸 위치 원본과 기관까지 거리 (ADR-006: 승인 판단·반경 재측정 기준).
 * 위치를 못 받은 수동 등록·관리자 직접 등록에서는 모두 null이다.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GeoPoint {

	private Double latitude;

	private Double longitude;

	// 기기가 알려 준 위치 정확도 (m)
	private Double accuracy;

	// 기관 좌표까지 거리 (m)
	@Column(name = "distance_meters")
	private Integer distanceMeters;

	public GeoPoint(double latitude, double longitude, Double accuracy, int distanceMeters) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.accuracy = accuracy;
		this.distanceMeters = distanceMeters;
	}

	public static GeoPoint empty() {
		return new GeoPoint();
	}
}
