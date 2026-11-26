package com.mongkids.domain.attendance.entity;

import com.mongkids.global.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 기관 사이 차량 이동 시간 (EMP-FR-023). 길찾기 API로 한 번 조회한 값을 방향별로 저장해 다시 쓴다.
 * 기관 좌표를 바꾸거나 기관을 지우면 그 기관이 들어간 값을 지운다.
 */
@Entity
@Table(
	name = "travel_time",
	uniqueConstraints = @UniqueConstraint(name = "uk_travel_time_route",
		columnNames = {"from_institution_id", "to_institution_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelTime extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "from_institution_id", nullable = false)
	private Institution from;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "to_institution_id", nullable = false)
	private Institution to;

	@Column(nullable = false)
	private int durationSeconds;

	@Column(nullable = false)
	private int distanceMeters;

	@Builder
	private TravelTime(Institution from, Institution to, int durationSeconds, int distanceMeters) {
		this.from = from;
		this.to = to;
		this.durationSeconds = durationSeconds;
		this.distanceMeters = distanceMeters;
	}
}
