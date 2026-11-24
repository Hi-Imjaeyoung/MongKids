package com.mongkids.domain.attendance.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import com.mongkids.domain.member.entity.Member;
import com.mongkids.global.common.BaseTimeEntity;

import jakarta.persistence.Column;
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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 직원에게 할당된 날짜별 수업 한 회 (EMP-FR-004). 수업 시작·종료 기록은 이 수업마다 1건이다.
 * 담당 직원은 비워 둘 수 있다(미정, EMP-FR-021). 미정 수업은 직원 화면·하루 시작·종료 판정에 들어가지 않는다.
 */
@Entity
@Table(
	name = "class_session",
	indexes = @Index(name = "idx_class_session_staff_date", columnList = "staff_id, class_date")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassSession extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "institution_id", nullable = false)
	private Institution institution;

	// null이면 직원 미정 (EMP-FR-021)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "staff_id")
	private Member staff;

	@Column(nullable = false)
	private LocalDate classDate;

	@Column(nullable = false)
	private LocalTime startTime;

	@Column(nullable = false)
	private LocalTime endTime;

	// 수업 대상 (반 이름 등)
	@Column(length = 50)
	private String target;

	// 정기 수업 / 행사·특별활동 (EMP-FR-004)
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "varchar(20)")
	private ClassType classType;

	@Builder
	private ClassSession(Institution institution, Member staff, LocalDate classDate, LocalTime startTime,
		LocalTime endTime, String target, ClassType classType) {
		this.institution = institution;
		this.staff = staff;
		this.classDate = classDate;
		this.startTime = startTime;
		this.endTime = endTime;
		this.target = target;
		this.classType = classType == null ? ClassType.REGULAR : classType;
	}

	public boolean isAssignedTo(Long memberId) {
		return staff != null && staff.getId().equals(memberId);
	}

	public boolean isUnassigned() {
		return staff == null;
	}

	/**
	 * 미정 수업에 직원을 배정한다 (EMP-FR-021 일괄 배정).
	 */
	public void assignStaff(Member staff) {
		this.staff = staff;
	}

	public void update(Institution institution, Member staff, LocalDate classDate, LocalTime startTime,
		LocalTime endTime, String target, ClassType classType) {
		this.institution = institution;
		this.staff = staff;
		this.classDate = classDate;
		this.startTime = startTime;
		this.endTime = endTime;
		this.target = target;
		this.classType = classType == null ? ClassType.REGULAR : classType;
	}

	// 같은 날 시간이 겹치는지 (끝 시각과 다음 시작 시각이 같으면 겹치지 않음)
	public boolean overlaps(LocalDate date, LocalTime start, LocalTime end) {
		return classDate.equals(date) && startTime.isBefore(end) && start.isBefore(endTime);
	}
}
