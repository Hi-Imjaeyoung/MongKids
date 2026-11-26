package com.mongkids.domain.attendance.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import com.mongkids.domain.attendance.entity.ClassSeries;
import com.mongkids.domain.attendance.entity.ClassType;

/**
 * 오늘 이후 직원 미정 회차가 남은 정기 수업 한 줄 (EMP-SCR-007).
 *
 * @param unassignedCount 오늘 이후 미정 회차 수
 * @param nextDate        가장 가까운 미정 회차 날짜
 * @param lastUnassignedDate 마지막 미정 회차 날짜
 */
public record UnassignedSeriesResponse(
	Long seriesId,
	Long institutionId,
	String institutionName,
	String target,
	ClassType classType,
	Set<DayOfWeek> daysOfWeek,
	LocalTime startTime,
	LocalTime endTime,
	LocalDate firstDate,
	LocalDate lastDate,
	long unassignedCount,
	LocalDate nextDate,
	LocalDate lastUnassignedDate
) {

	public static UnassignedSeriesResponse of(ClassSeries series, long unassignedCount, LocalDate nextDate,
		LocalDate lastUnassignedDate) {
		return new UnassignedSeriesResponse(series.getId(), series.getInstitution().getId(),
			series.getInstitution().getName(), series.getTarget(), series.getClassType(), series.getDays(),
			series.getStartTime(), series.getEndTime(), series.getFirstDate(), series.getLastDate(), unassignedCount,
			nextDate, lastUnassignedDate);
	}
}
