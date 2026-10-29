package com.mongkids.domain.attendance.entity;

import java.time.LocalDateTime;

import com.mongkids.domain.member.entity.Member;
import com.mongkids.global.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
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
 * 위치로 수업 시작을 확인한 시도마다 남기는 재조정 로그 (EMP-FR-001, ADR-006).
 * 수업 시작 기록과 달리 덮어쓰지 않고, 반경 기준을 다시 잴 때만 쓴다.
 */
@Entity
@Table(
	name = "class_start_attempt",
	indexes = @Index(name = "idx_class_start_attempt_session", columnList = "class_session_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassStartAttempt extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "class_session_id", nullable = false)
	private ClassSession classSession;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Embedded
	private GeoPoint location;

	// 반경 안이면 true (자동 확정)
	@Column(nullable = false)
	private boolean withinRadius;

	@Column(nullable = false)
	private LocalDateTime attemptedAt;

	public ClassStartAttempt(ClassSession classSession, Member member, GeoPoint location, boolean withinRadius,
		LocalDateTime attemptedAt) {
		this.classSession = classSession;
		this.member = member;
		this.location = location;
		this.withinRadius = withinRadius;
		this.attemptedAt = attemptedAt;
	}
}
