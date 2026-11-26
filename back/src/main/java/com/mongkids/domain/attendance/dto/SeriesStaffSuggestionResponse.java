package com.mongkids.domain.attendance.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 정기 수업 담당 직원 추천 (EMP-FR-022, EMP-SCR-007).
 *
 * @param series     추천 대상 정기 수업
 * @param weekCount  주 평균 계산에 쓴 주(월~일) 수
 * @param candidates 배정 가능한 직원, 추천 순서대로
 * @param unavailable 배정할 수 없는 직원과 사유, 이름순
 */
public record SeriesStaffSuggestionResponse(
	UnassignedSeriesResponse series,
	int weekCount,
	List<Candidate> candidates,
	List<Unavailable> unavailable
) {

	/**
	 * @param rank              1부터
	 * @param classCount        추천 기간 동안 이미 맡은 수업 수 (정기·행사)
	 * @param weeklyAverage     주 평균 수업 수 (소수 첫째 자리)
	 * @param extraTravelAverage 회차 평균 추가 이동 시간 (분, 소수 첫째 자리)
	 * @param extraTravelMax    가장 긴 회차의 추가 이동 시간 (분)
	 * @param neighbors         앞·뒤 수업 경우별 회수
	 */
	public record Candidate(
		int rank,
		Long staffId,
		String staffName,
		long classCount,
		double weeklyAverage,
		double extraTravelAverage,
		int extraTravelMax,
		List<Neighbor> neighbors
	) {
	}

	/**
	 * 해당 수업 바로 앞(BEFORE)이나 뒤(AFTER) 수업.
	 *
	 * @param travelMinutes 해당 수업 기관까지(BEFORE) 또는 해당 수업 기관에서(AFTER) 이동 시간
	 * @param arrivalTime   BEFORE: 해당 수업 기관 도착 시각, AFTER: 뒤 수업 기관 도착 시각
	 * @param count         이 경우인 회차 수
	 */
	public record Neighbor(
		Position position,
		String institutionName,
		LocalTime startTime,
		LocalTime endTime,
		int travelMinutes,
		LocalTime arrivalTime,
		long count
	) {
	}

	public enum Position {
		BEFORE,
		AFTER
	}

	public record Unavailable(
		Long staffId,
		String staffName,
		List<Reason> reasons
	) {
	}

	/**
	 * 사유별 회수와 날짜. 상세 값은 첫 회차 기준이다.
	 *
	 * @param institutionName  OVERLAP: 겹치는 수업 기관, TRAVEL_SHORT: 앞·뒤 수업 기관
	 * @param requiredMinutes  TRAVEL_SHORT: 필요한 이동 시간
	 * @param availableMinutes TRAVEL_SHORT: 3분 전 도착 기준 여유 시간
	 */
	public record Reason(
		ReasonType type,
		int count,
		List<LocalDate> dates,
		String institutionName,
		LocalTime startTime,
		LocalTime endTime,
		Integer requiredMinutes,
		Integer availableMinutes
	) {
	}

	public enum ReasonType {
		// 같은 시간에 다른 수업이 있음
		OVERLAP,
		// 앞·뒤 수업에서 이동해 3분 전까지 도착할 수 없음
		TRAVEL_SHORT,
		// 하루 시작 기록이 있어 그날 수업을 바꿀 수 없음
		DAY_LOCKED
	}
}
