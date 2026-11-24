package com.mongkids.domain.attendance.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.mongkids.domain.attendance.entity.ClassStartMethod;
import com.mongkids.domain.attendance.entity.ClassStartStatus;

/**
 * 관리자 근무 캘린더 (EMP-FR-017, EMP-SCR-006). 기간 안에 수업이 있는 직원만 이름순으로 담는다.
 * 근무 시간은 계산하지 않고 수업 횟수만 집계한다. {@code unassigned}는 직원 미정 수업이 있는 날(EMP-FR-021)이다.
 */
public record WorkCalendarResponse(
	LocalDate from,
	LocalDate to,
	List<StaffCalendarResponse> staffs,
	List<UnassignedDayResponse> unassigned
) {

	/**
	 * 직원 미정 수업이 있는 날과 그날 미정 수업 수 (EMP-FR-021). 날짜순.
	 */
	public record UnassignedDayResponse(
		LocalDate date,
		int classCount
	) {
	}

	/**
	 * 직원 한 명의 기간 안 하루들(수업 있는 날만, 날짜순)과 기간 합계.
	 */
	public record StaffCalendarResponse(
		Long staffId,
		String staffName,
		List<CalendarDayResponse> days,
		CalendarSummaryResponse summary
	) {
	}

	/**
	 * 캘린더 칸 하나. {@code legacy}면 하루 단위 적용 전 날짜라 상태·시작·종료 대신 수업 단위 기록으로 집계한다.
	 * {@code institutionNames}는 그날 수업한 기관을 수업 순서대로 한 번씩 담는다.
	 */
	public record CalendarDayResponse(
		LocalDate date,
		DayStatus status,
		boolean legacy,
		int classCount,
		// 그 가운데 행사·특별활동 수 (EMP-FR-004)
		int eventCount,
		LocalTime firstClassTime,
		CalendarCheckResponse start,
		CalendarCheckResponse end,
		List<String> institutionNames
	) {
	}

	/**
	 * 하루 시작·종료 기록 요약. 칸에는 실제 시각과 수동·관리자 등록 여부만 보여 준다.
	 */
	public record CalendarCheckResponse(
		ClassStartMethod method,
		ClassStartStatus status,
		LocalDateTime checkedAt
	) {

		public static CalendarCheckResponse from(DayCheckResponse check) {
			return check == null ? null : new CalendarCheckResponse(check.method(), check.status(), check.checkedAt());
		}
	}

	/**
	 * 기간 합계. 할당 수업은 앞으로 예정 포함, 완료 수업은 수업 종료(ENDED)인 날의 수업 수,
	 * 승인 대기·반려는 하루 시작·종료 기록 건수다. 하루 단위 적용 전 날짜는 수업 단위 기록을 수업마다 센다.
	 */
	public record CalendarSummaryResponse(
		int assignedClasses,
		// 할당 수업 가운데 행사·특별활동 수. 정기 수업 수는 할당 - 행사
		int eventClasses,
		int completedClasses,
		int pendingChecks,
		int rejectedChecks
	) {
	}
}
