package com.mongkids.domain.attendance.entity;

import java.time.LocalDate;

import com.mongkids.domain.member.entity.Member;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 하루 시작 기록 (EMP-FR-020). 그날 첫 수업 기관 좌표와 대조한다.
 * 이 기록이 있는 직원·날짜의 수업 할당은 추가·수정·삭제할 수 없다 (EMP-FR-004).
 */
@Entity
@Table(
	name = "day_start",
	uniqueConstraints = @UniqueConstraint(name = "uk_day_start_staff_date", columnNames = {"staff_id", "work_date"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DayStart extends DayCheck {

	private DayStart(Member staff, LocalDate workDate) {
		super(staff, workDate);
	}

	public static DayStart of(Member staff, LocalDate workDate) {
		return new DayStart(staff, workDate);
	}
}
