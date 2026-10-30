package com.mongkids.domain.attendance.entity;

import com.mongkids.global.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 수업하는 기관과 위치 (EMP-FR-012). 좌표는 수업 시작 판정(EMP-FR-001)의 기준이다.
 * 관리자 화면(EMP-SCR-006)에서 등록·수정한다. 좌표를 바꿔도 이미 저장된 수업 시작 기록의 거리는 그대로 둔다.
 */
@Entity
@Table(name = "institution")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Institution extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false, length = 255)
	private String address;

	@Column(nullable = false)
	private double latitude;

	@Column(nullable = false)
	private double longitude;

	@Builder
	private Institution(String name, String address, double latitude, double longitude) {
		this.name = name;
		this.address = address;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public void update(String name, String address, double latitude, double longitude) {
		this.name = name;
		this.address = address;
		this.latitude = latitude;
		this.longitude = longitude;
	}
}
