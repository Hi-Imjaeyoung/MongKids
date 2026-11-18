package com.mongkids.domain.attendance.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.mongkids.domain.member.entity.Member;
import com.mongkids.global.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 하루 시작·종료를 위치로 확인한 시도마다 남기는 재조정 로그 (EMP-FR-020, ADR-006).
 * 기록과 달리 덮어쓰지 않고, 반경 기준을 다시 잴 때만 쓴다.
 */
@Entity
@Table(
	name = "day_check_attempt",
	indexes = @Index(name = "idx_day_check_attempt_staff_date", columnList = "staff_id, work_date")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DayCheckAttempt extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "staff_id", nullable = false)
	private Member staff;

	@Column(nullable = false)
	private LocalDate workDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "varchar(20)")
	private DayCheckType type;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "institution_id", nullable = false)
	private Institution institution;

	@Embedded
	private GeoPoint location;

	// 반경 안이면 true
	@Column(nullable = false)
	private boolean withinRadius;

	@Column(nullable = false)
	private LocalDateTime attemptedAt;

	public DayCheckAttempt(Member staff, LocalDate workDate, DayCheckType type, Institution institution,
		GeoPoint location, boolean withinRadius, LocalDateTime attemptedAt) {
		this.staff = staff;
		this.workDate = workDate;
		this.type = type;
		this.institution = institution;
		this.location = location;
		this.withinRadius = withinRadius;
		this.attemptedAt = attemptedAt;
	}
}
