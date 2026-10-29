package com.mongkids.domain.attendance.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import com.mongkids.domain.member.entity.Member;
import com.mongkids.global.common.BaseTimeEntity;

import jakarta.persistence.Column;
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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 직원에게 할당된 날짜별 수업 한 회 (EMP-FR-004). 수업 시작·종료 기록은 이 수업마다 1건이다.
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

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "staff_id", nullable = false)
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

	@Builder
	private ClassSession(Institution institution, Member staff, LocalDate classDate, LocalTime startTime,
		LocalTime endTime, String target) {
		this.institution = institution;
		this.staff = staff;
		this.classDate = classDate;
		this.startTime = startTime;
		this.endTime = endTime;
		this.target = target;
	}

	public boolean isAssignedTo(Long memberId) {
		return staff.getId().equals(memberId);
	}
}
