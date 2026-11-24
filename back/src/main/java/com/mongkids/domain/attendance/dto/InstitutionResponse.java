package com.mongkids.domain.attendance.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.mongkids.domain.attendance.entity.Institution;

/**
 * 기관과 위치 (EMP-FR-012). 삭제하면 수업({@code classSessionCount})과 기록을 함께 지운다.
 * 기관 표(EMP-SCR-006)에 보여 줄 앞으로 수업 요약을 함께 담는다.
 *
 * @param classSessionCount 지난 수업을 포함한 전체 할당 수업
 * @param upcomingClassCount 아직 끝나지 않은 수업 (오늘 남은 수업 포함)
 * @param nextClass 가장 먼저 다가오는 수업. 없으면 null
 * @param schedules 앞으로 수업을 시간대별로 묶은 요일 (예: 월·수 10:00–11:00)
 */
public record InstitutionResponse(
	Long id,
	String name,
	String address,
	double latitude,
	double longitude,
	long classSessionCount,
	long upcomingClassCount,
	NextClass nextClass,
	List<Schedule> schedules
) {

	/**
	 * @param upcoming 아직 끝나지 않은 이 기관 수업, 날짜·시작 시각 순
	 */
	public static InstitutionResponse of(Institution institution, long classSessionCount, List<UpcomingClass> upcoming) {
		NextClass next = upcoming.isEmpty() ? null
			: new NextClass(upcoming.get(0).classDate(), upcoming.get(0).startTime());
		return new InstitutionResponse(institution.getId(), institution.getName(), institution.getAddress(),
			institution.getLatitude(), institution.getLongitude(), classSessionCount, upcoming.size(), next,
			schedules(upcoming));
	}

	// 같은 시작·끝 시각끼리 요일을 모은다. 첫 요일, 시작 시각 순
	private static List<Schedule> schedules(List<UpcomingClass> upcoming) {
		Map<List<LocalTime>, TreeSet<DayOfWeek>> byTime = new LinkedHashMap<>();
		upcoming.forEach(item -> byTime
			.computeIfAbsent(List.of(item.startTime(), item.endTime()), key -> new TreeSet<>())
			.add(item.classDate().getDayOfWeek()));
		return byTime.entrySet().stream()
			.map(entry -> new Schedule(List.copyOf(entry.getValue()), entry.getKey().get(0), entry.getKey().get(1)))
			.sorted(Comparator.comparing((Schedule schedule) -> schedule.daysOfWeek().get(0))
				.thenComparing(Schedule::startTime))
			.toList();
	}

	public record UpcomingClass(LocalDate classDate, LocalTime startTime, LocalTime endTime) {
	}

	public record NextClass(LocalDate classDate, LocalTime startTime) {
	}

	public record Schedule(List<DayOfWeek> daysOfWeek, LocalTime startTime, LocalTime endTime) {
	}
}
