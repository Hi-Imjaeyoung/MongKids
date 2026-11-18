package com.mongkids.domain.attendance.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.mongkids.domain.member.entity.Member;
import com.mongkids.global.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 하루 시작·종료 기록의 공통 부분 (EMP-FR-020). 직원·날짜마다 1건이고, 반려된 기록만 다시 확인해 덮어쓴다.
 * 판정·수동 등록·승인 방식은 수업 단위 기록(EMP-FR-001, 011)과 같다 (ADR-006).
 */
@MappedSuperclass
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class DayCheck extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "staff_id", nullable = false)
	private Member staff;

	// 한국 시간 기준 그날
	@Column(nullable = false)
	private LocalDate workDate;

	// 위치를 대조한 기관 (시작: 첫 수업 기관, 종료: 마지막 수업 기관)
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "institution_id", nullable = false)
	private Institution institution;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "varchar(20)")
	private ClassStartMethod method;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "varchar(20)")
	private ClassStartStatus status;

	@Embedded
	private GeoPoint location;

	// 서버 시각
	@Column(nullable = false)
	private LocalDateTime checkedAt;

	// 수동 등록·관리자 직접 등록 사유
	@Column(length = 200)
	private String reason;

	@Column(length = 200)
	private String rejectReason;

	// 승인·반려·직접 등록한 관리자
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "processed_by")
	private Member processedBy;

	private LocalDateTime processedAt;

	protected DayCheck(Member staff, LocalDate workDate) {
		this.staff = staff;
		this.workDate = workDate;
	}

	/**
	 * 반경 안에서 위치로 확인해 자동 확정한다.
	 */
	public void confirmByLocation(Institution institution, GeoPoint location, LocalDateTime now) {
		reset(institution, ClassStartMethod.LOCATION, ClassStartStatus.CONFIRMED, location, null, now);
	}

	/**
	 * 위치로 확인하지 못해 사유를 적어 수동 등록한다. 관리자 승인 전까지 승인 대기다.
	 */
	public void registerManually(Institution institution, GeoPoint location, String reason, LocalDateTime now) {
		reset(institution, ClassStartMethod.MANUAL, ClassStartStatus.PENDING, location, reason, now);
	}

	public void registerByAdmin(Institution institution, Member admin, String reason, LocalDateTime now) {
		reset(institution, ClassStartMethod.ADMIN, ClassStartStatus.CONFIRMED, GeoPoint.empty(), reason, now);
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

	public boolean isRejected() {
		return status == ClassStartStatus.REJECTED;
	}

	public boolean isPending() {
		return status == ClassStartStatus.PENDING;
	}

	// 반려된 기록을 덮어쓸 때 이전 처리 내용도 지운다
	private void reset(Institution institution, ClassStartMethod method, ClassStartStatus status, GeoPoint location,
		String reason, LocalDateTime now) {
		this.institution = institution;
		this.method = method;
		this.status = status;
		this.location = location;
		this.reason = reason;
		this.checkedAt = now;
		this.rejectReason = null;
		this.processedBy = null;
		this.processedAt = null;
	}
}
