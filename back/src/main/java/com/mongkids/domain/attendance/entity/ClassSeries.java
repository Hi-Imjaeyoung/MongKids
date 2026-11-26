package com.mongkids.domain.attendance.entity;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 정기 수업(수업 묶음): 반복 등록 한 건 (EMP-FR-024). 반복 등록으로 만든 수업 회차가 이 묶음을 가리킨다.
 * 담당 직원은 묶음 단위 배정(EMP-FR-022) 때 바뀐다. 한 회만 바꾸는 수정은 회차에만 반영하고 묶음은 그대로 둔다.
 * 이 테이블이 생기기 전에 등록된 수업은 묶음이 없다.
 */
@Entity
@Table(name = "class_series")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassSeries extends BaseTimeEntity {

	private static final String DAY_SEPARATOR = ",";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "institution_id", nullable = false)
	private Institution institution;

	// null이면 직원 미정
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "staff_id")
	private Member staff;

	@Column(length = 50)
	private String target;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "varchar(20)")
	private ClassType classType;

	// 요일 이름을 쉼표로 이어 저장 (예: MONDAY,WEDNESDAY)
	@Column(nullable = false, length = 70)
	private String daysOfWeek;

	@Column(nullable = false)
	private LocalTime startTime;

	@Column(nullable = false)
	private LocalTime endTime;

	@Column(nullable = false)
	private LocalDate firstDate;

	@Column(nullable = false)
	private LocalDate lastDate;

	@Builder
	private ClassSeries(Institution institution, Member staff, String target, ClassType classType,
		Collection<DayOfWeek> daysOfWeek, LocalTime startTime, LocalTime endTime, LocalDate firstDate,
		LocalDate lastDate) {
		this.institution = institution;
		this.staff = staff;
		this.target = target;
		this.classType = classType == null ? ClassType.REGULAR : classType;
		this.daysOfWeek = EnumSet.copyOf(daysOfWeek).stream().map(DayOfWeek::name)
			.collect(Collectors.joining(DAY_SEPARATOR));
		this.startTime = startTime;
		this.endTime = endTime;
		this.firstDate = firstDate;
		this.lastDate = lastDate;
	}

	public Set<DayOfWeek> getDays() {
		return Arrays.stream(daysOfWeek.split(DAY_SEPARATOR)).map(DayOfWeek::valueOf)
			.collect(Collectors.toCollection(() -> EnumSet.noneOf(DayOfWeek.class)));
	}

	public void assignStaff(Member staff) {
		this.staff = staff;
	}
}
