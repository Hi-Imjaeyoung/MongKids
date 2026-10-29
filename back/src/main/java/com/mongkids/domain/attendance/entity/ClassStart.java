package com.mongkids.domain.attendance.entity;

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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 수업 시작 기록 (EMP-FR-001, EMP-FR-011). 수업당 1건이고, 반려된 기록만 다시 확인해 덮어쓴다.
 */
@Entity
@Table(name = "class_start")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassStart extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "class_session_id", nullable = false, unique = true)
	private ClassSession classSession;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "varchar(20)")
	private ClassStartMethod method;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "varchar(20)")
	private ClassStartStatus status;

	@Embedded
	private GeoPoint location;

	// 서버 시각 (ADR-006: 시작 시간 제한 없음)
	@Column(nullable = false)
	private LocalDateTime startedAt;

	// 수동 등록·관리자 직접 등록 사유
	@Column(length = 200)
	private String reason;

	@Column(length = 200)
	private String rejectReason;

	// 수업 시작 특이사항 (EMP-FR-002)
	@Column(length = 1000)
	private String note;

	// 승인·반려·직접 등록한 관리자
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "processed_by")
	private Member processedBy;

	private LocalDateTime processedAt;

	private ClassStart(ClassSession classSession) {
		this.classSession = classSession;
	}

	public static ClassStart of(ClassSession classSession) {
		return new ClassStart(classSession);
	}

	/**
	 * 반경 안에서 위치로 확인해 자동 확정한다.
	 */
	public void confirmByLocation(GeoPoint location, LocalDateTime now) {
		reset(ClassStartMethod.LOCATION, ClassStartStatus.CONFIRMED, location, null, now);
	}

	/**
	 * 위치로 확인하지 못해 사유를 적어 수동 등록한다. 관리자 승인 전까지 승인 대기다.
	 */
	public void registerManually(GeoPoint location, String reason, LocalDateTime now) {
		reset(ClassStartMethod.MANUAL, ClassStartStatus.PENDING, location, reason, now);
	}

	public void registerByAdmin(Member admin, String reason, LocalDateTime now) {
		reset(ClassStartMethod.ADMIN, ClassStartStatus.CONFIRMED, GeoPoint.empty(), reason, now);
		this.processedBy = admin;
		this.processedAt = now;
	}

	public void approve(Member admin, LocalDateTime now) {
		this.status = ClassStartStatus.CONFIRMED;
		this.processedBy = admin;
		this.processedAt = now;
	}

	public void reject(Member admin, String rejectReason, LocalDateTime now) {
		this.status = ClassStartStatus.REJECTED;
		this.rejectReason = rejectReason;
		this.processedBy = admin;
		this.processedAt = now;
	}

	public void saveNote(String note) {
		this.note = note;
	}

	public boolean isRejected() {
		return status == ClassStartStatus.REJECTED;
	}

	public boolean isPending() {
		return status == ClassStartStatus.PENDING;
	}

	// 반려된 기록을 덮어쓸 때 이전 처리 내용과 특이사항도 지운다
	private void reset(ClassStartMethod method, ClassStartStatus status, GeoPoint location, String reason,
		LocalDateTime now) {
		this.method = method;
		this.status = status;
		this.location = location;
		this.reason = reason;
		this.startedAt = now;
		this.rejectReason = null;
		this.note = null;
		this.processedBy = null;
		this.processedAt = null;
	}
}
