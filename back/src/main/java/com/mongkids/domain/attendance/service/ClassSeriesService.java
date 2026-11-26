package com.mongkids.domain.attendance.service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mongkids.domain.attendance.dto.AdminClassSessionResponse;
import com.mongkids.domain.attendance.dto.ClassSessionAssignRequest;
import com.mongkids.domain.attendance.dto.SeriesStaffAssignRequest;
import com.mongkids.domain.attendance.dto.SeriesStaffSuggestionResponse;
import com.mongkids.domain.attendance.dto.SeriesStaffSuggestionResponse.Candidate;
import com.mongkids.domain.attendance.dto.SeriesStaffSuggestionResponse.Neighbor;
import com.mongkids.domain.attendance.dto.SeriesStaffSuggestionResponse.Position;
import com.mongkids.domain.attendance.dto.SeriesStaffSuggestionResponse.Reason;
import com.mongkids.domain.attendance.dto.SeriesStaffSuggestionResponse.ReasonType;
import com.mongkids.domain.attendance.dto.SeriesStaffSuggestionResponse.Unavailable;
import com.mongkids.domain.attendance.dto.UnassignedSeriesResponse;
import com.mongkids.domain.attendance.entity.ClassSeries;
import com.mongkids.domain.attendance.entity.ClassSession;
import com.mongkids.domain.attendance.entity.Institution;
import com.mongkids.domain.attendance.repository.ClassSeriesRepository;
import com.mongkids.domain.attendance.repository.ClassSessionRepository;
import com.mongkids.domain.attendance.repository.ClassSessionRepository.SeriesUnassignedCount;
import com.mongkids.domain.attendance.repository.DayStartRepository;
import com.mongkids.domain.attendance.service.TravelTimeService.Route;
import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 정기 수업(묶음) 단위 담당 직원 추천·배정 (EMP-FR-022, EMP-SCR-007).
 * 대상은 묶음의 오늘 이후 미정 회차다. 지난 미정 회차는 기존처럼 한 회씩 배정한다(EMP-FR-021).
 * 직원의 출발지·귀가지는 보지 않는다. 그날 첫·마지막 수업이면 그쪽 이동은 계산하지 않는다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ClassSeriesService {

	// 수업 시작 몇 분 전까지 도착해야 하는지
	static final int ARRIVAL_MARGIN_MINUTES = 3;
	private static final int DAYS_PER_WEEK = 7;

	private final ClassSeriesRepository classSeriesRepository;
	private final ClassSessionRepository classSessionRepository;
	private final DayStartRepository dayStartRepository;
	private final ClassSessionService classSessionService;
	private final TravelTimeService travelTimeService;
	private final MemberService memberService;
	private final Clock clock;

	/**
	 * 오늘 이후 미정 회차가 남은 정기 수업. 가까운 미정 회차 순이다.
	 */
	public List<UnassignedSeriesResponse> getUnassignedSeries(Long adminId) {
		memberService.validateAdmin(adminId);
		LocalDateTime now = LocalDateTime.now(clock);
		Map<Long, SeriesUnassignedCount> counts = classSessionRepository
			.countUnassignedUpcomingBySeries(now.toLocalDate(), now.toLocalTime()).stream()
			.collect(Collectors.toMap(SeriesUnassignedCount::getSeriesId, Function.identity()));
		if (counts.isEmpty()) {
			return List.of();
		}
		return classSeriesRepository.findByIdIn(counts.keySet()).stream()
			.map(series -> {
				SeriesUnassignedCount count = counts.get(series.getId());
				return UnassignedSeriesResponse.of(series, count.getUnassignedCount(), count.getNextDate(),
					count.getLastDate());
			})
			.sorted(Comparator.comparing(UnassignedSeriesResponse::nextDate)
				.thenComparing(UnassignedSeriesResponse::startTime)
				.thenComparing(UnassignedSeriesResponse::seriesId))
			.toList();
	}

	/**
	 * 담당 직원 추천. 오늘 이후 모든 미정 회차에서 시간이 겹치지 않고, 그날 앞·뒤 수업에서 차로 이동해
	 * 수업 시작 3분 전까지 도착할 수 있는 직원만 후보다. 한 회라도 안 되면 사유와 함께 따로 돌려준다.
	 * 순위는 남은 기간 주 평균 수업 수가 적은 순, 같으면 회차 평균 추가 이동 시간이 짧은 순이다.
	 * 처음 쓰는 구간은 길찾기로 이동 시간을 조회해 저장하므로 쓰기 트랜잭션이다.
	 */
	@Transactional
	public SeriesStaffSuggestionResponse getStaffSuggestions(Long adminId, Long seriesId) {
		memberService.validateAdmin(adminId);
		ClassSeries series = getSeries(seriesId);
		LocalDateTime now = LocalDateTime.now(clock);
		List<ClassSession> targets = getUpcomingTargets(seriesId, now);
		LocalDate weekStart = targets.get(0).getClassDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate weekEnd = targets.get(targets.size() - 1).getClassDate()
			.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
		int weekCount = (int)((ChronoUnit.DAYS.between(weekStart, weekEnd) + 1) / DAYS_PER_WEEK);

		List<Member> staffs = memberService.getActiveStaffs();
		Map<Long, List<ClassSession>> sessionsByStaff = staffs.isEmpty() ? Map.of()
			: classSessionRepository.findByStaffIdInAndClassDateBetween(
					staffs.stream().map(Member::getId).toList(), weekStart, weekEnd).stream()
				.collect(Collectors.groupingBy(session -> session.getStaff().getId()));

		// 1단계: 회차마다 겹치는 수업과 앞·뒤 수업을 찾고, 필요한 이동 구간을 모은다
		Map<Long, List<Placement>> placements = new LinkedHashMap<>();
		Set<Route> routes = new HashSet<>();
		for (Member staff : staffs) {
			Map<LocalDate, List<ClassSession>> byDate = sessionsByStaff.getOrDefault(staff.getId(), List.of()).stream()
				.collect(Collectors.groupingBy(ClassSession::getClassDate));
			List<Placement> list = new ArrayList<>();
			for (ClassSession target : targets) {
				Placement placement = Placement.of(target, byDate.getOrDefault(target.getClassDate(), List.of()));
				placement.routes().forEach(routes::add);
				list.add(placement);
			}
			placements.put(staff.getId(), list);
		}
		Map<Route, Integer> minutes = travelTimeService.getMinutes(routes);
		LocalDate today = now.toLocalDate();
		boolean includesToday = targets.stream().anyMatch(target -> target.getClassDate().equals(today));

		// 2단계: 직원별로 조건을 확인하고 추천 값을 계산한다
		List<Evaluation> candidates = new ArrayList<>();
		List<Unavailable> unavailable = new ArrayList<>();
		for (Member staff : staffs) {
			boolean lockedToday = includesToday && dayStartRepository.existsByStaffIdAndWorkDate(staff.getId(), today);
			Evaluation evaluation = evaluate(staff, placements.get(staff.getId()), minutes, lockedToday, today,
				sessionsByStaff.getOrDefault(staff.getId(), List.of()).size());
			if (evaluation.reasons().isEmpty()) {
				candidates.add(evaluation);
			} else {
				unavailable.add(new Unavailable(staff.getId(), staff.getName(), evaluation.reasons()));
			}
		}
		candidates.sort(Comparator.comparingLong(Evaluation::classCount)
			.thenComparingLong(Evaluation::extraTravelSum)
			.thenComparing(evaluation -> evaluation.staff().getName())
			.thenComparing(evaluation -> evaluation.staff().getId()));

		List<Candidate> ranked = new ArrayList<>();
		for (int i = 0; i < candidates.size(); i++) {
			Evaluation evaluation = candidates.get(i);
			ranked.add(new Candidate(i + 1, evaluation.staff().getId(), evaluation.staff().getName(),
				evaluation.classCount(), round1((double)evaluation.classCount() / weekCount),
				round1((double)evaluation.extraTravelSum() / targets.size()), evaluation.extraTravelMax(),
				evaluation.neighbors()));
		}
		ClassSession last = targets.get(targets.size() - 1);
		return new SeriesStaffSuggestionResponse(
			UnassignedSeriesResponse.of(series, targets.size(), targets.get(0).getClassDate(), last.getClassDate()),
			weekCount, ranked, unavailable);
	}

	/**
	 * 오늘 이후 미정 회차를 직원 한 명에게 배정하고 정기 수업의 담당 직원도 바꾼다.
	 * 겹침·하루 시작 잠금 검사는 일괄 배정(EMP-FR-021)과 같고, 하나라도 걸리면 하나도 배정하지 않는다.
	 */
	@Transactional
	public List<AdminClassSessionResponse> assignStaff(Long adminId, Long seriesId, SeriesStaffAssignRequest request) {
		memberService.validateAdmin(adminId);
		ClassSeries series = getSeries(seriesId);
		List<Long> ids = getUpcomingTargets(seriesId, LocalDateTime.now(clock)).stream()
			.map(ClassSession::getId)
			.toList();
		List<AdminClassSessionResponse> assigned = classSessionService.assignStaff(adminId,
			new ClassSessionAssignRequest(ids, request.staffId()));
		series.assignStaff(memberService.getActiveMember(request.staffId()));
		return assigned;
	}

	private ClassSeries getSeries(Long seriesId) {
		return classSeriesRepository.findWithInstitutionById(seriesId)
			.orElseThrow(() -> new BusinessException(ErrorCode.CLASS_SERIES_NOT_FOUND));
	}

	private List<ClassSession> getUpcomingTargets(Long seriesId, LocalDateTime now) {
		List<ClassSession> targets = classSessionRepository.findUnassignedUpcomingBySeries(seriesId,
			now.toLocalDate(), now.toLocalTime());
		if (targets.isEmpty()) {
			throw new BusinessException(ErrorCode.NO_UNASSIGNED_UPCOMING_CLASS);
		}
		return targets;
	}

	private static Evaluation evaluate(Member staff, List<Placement> placements, Map<Route, Integer> minutes,
		boolean lockedToday, LocalDate today, long classCount) {
		Map<ReasonType, List<Issue>> issues = new EnumMap<>(ReasonType.class);
		Map<NeighborKey, Long> neighbors = new LinkedHashMap<>();
		long extraSum = 0;
		int extraMax = 0;
		for (Placement placement : placements) {
			ClassSession target = placement.target();
			if (placement.overlap() != null) {
				issues.computeIfAbsent(ReasonType.OVERLAP, type -> new ArrayList<>())
					.add(new Issue(target.getClassDate(), placement.overlap(), null, null));
				continue;
			}
			if (lockedToday && target.getClassDate().equals(today)) {
				issues.computeIfAbsent(ReasonType.DAY_LOCKED, type -> new ArrayList<>())
					.add(new Issue(target.getClassDate(), null, null, null));
				continue;
			}
			ClassSession prev = placement.prev();
			ClassSession next = placement.next();
			int before = prev == null ? 0 : minutes.get(new Route(prev.getInstitution(), target.getInstitution()));
			int after = next == null ? 0 : minutes.get(new Route(target.getInstitution(), next.getInstitution()));
			boolean travelShort = false;
			if (prev != null) {
				int available = minutesBetween(prev.getEndTime(), target.getStartTime()) - ARRIVAL_MARGIN_MINUTES;
				if (before > available) {
					issues.computeIfAbsent(ReasonType.TRAVEL_SHORT, type -> new ArrayList<>())
						.add(new Issue(target.getClassDate(), prev, before, available));
					travelShort = true;
				}
			}
			if (next != null && !travelShort) {
				int available = minutesBetween(target.getEndTime(), next.getStartTime()) - ARRIVAL_MARGIN_MINUTES;
				if (after > available) {
					issues.computeIfAbsent(ReasonType.TRAVEL_SHORT, type -> new ArrayList<>())
						.add(new Issue(target.getClassDate(), next, after, available));
					travelShort = true;
				}
			}
			if (travelShort) {
				continue;
			}
			int direct = prev != null && next != null
				? minutes.get(new Route(prev.getInstitution(), next.getInstitution())) : 0;
			int extra = Math.max(0, before + after - direct);
			extraSum += extra;
			extraMax = Math.max(extraMax, extra);
			if (prev != null) {
				neighbors.merge(new NeighborKey(Position.BEFORE, prev.getInstitution().getName(), prev.getStartTime(),
					prev.getEndTime(), before, prev.getEndTime().plusMinutes(before)), 1L, Long::sum);
			}
			if (next != null) {
				neighbors.merge(new NeighborKey(Position.AFTER, next.getInstitution().getName(), next.getStartTime(),
					next.getEndTime(), after, target.getEndTime().plusMinutes(after)), 1L, Long::sum);
			}
		}

		List<Reason> reasons = issues.entrySet().stream()
			.map(entry -> toReason(entry.getKey(), entry.getValue()))
			.toList();
		List<Neighbor> neighborList = neighbors.entrySet().stream()
			.map(entry -> entry.getKey().toNeighbor(entry.getValue()))
			.sorted(Comparator.comparing(Neighbor::position).thenComparing(Neighbor::count, Comparator.reverseOrder()))
			.toList();
		return new Evaluation(staff, classCount, extraSum, extraMax, neighborList, reasons);
	}

	private static Reason toReason(ReasonType type, List<Issue> issues) {
		Issue first = issues.get(0);
		ClassSession other = first.other();
		return new Reason(type, issues.size(), issues.stream().map(Issue::date).toList(),
			other == null ? null : other.getInstitution().getName(),
			other == null ? null : other.getStartTime(),
			other == null ? null : other.getEndTime(),
			first.requiredMinutes(), first.availableMinutes());
	}

	private static int minutesBetween(LocalTime from, LocalTime to) {
		return (int)Duration.between(from, to).toMinutes();
	}

	private static double round1(double value) {
		return Math.round(value * 10) / 10.0;
	}

	/**
	 * 한 직원의 그날 수업 사이에 해당 회차를 넣어 본 결과. 겹치면 앞·뒤 수업은 보지 않는다.
	 */
	private record Placement(ClassSession target, ClassSession overlap, ClassSession prev, ClassSession next) {

		static Placement of(ClassSession target, List<ClassSession> sameDay) {
			Optional<ClassSession> overlap = sameDay.stream()
				.filter(session -> session.overlaps(target.getClassDate(), target.getStartTime(), target.getEndTime()))
				.findFirst();
			if (overlap.isPresent()) {
				return new Placement(target, overlap.get(), null, null);
			}
			ClassSession prev = sameDay.stream()
				.filter(session -> !session.getEndTime().isAfter(target.getStartTime()))
				.max(Comparator.comparing(ClassSession::getEndTime).thenComparing(ClassSession::getStartTime))
				.orElse(null);
			ClassSession next = sameDay.stream()
				.filter(session -> !session.getStartTime().isBefore(target.getEndTime()))
				.min(Comparator.comparing(ClassSession::getStartTime).thenComparing(ClassSession::getEndTime))
				.orElse(null);
			return new Placement(target, null, prev, next);
		}

		List<Route> routes() {
			List<Route> routes = new ArrayList<>();
			Institution at = target.getInstitution();
			if (prev != null) {
				routes.add(new Route(prev.getInstitution(), at));
			}
			if (next != null) {
				routes.add(new Route(at, next.getInstitution()));
			}
			if (prev != null && next != null) {
				routes.add(new Route(prev.getInstitution(), next.getInstitution()));
			}
			return routes;
		}
	}

	private record Issue(LocalDate date, ClassSession other, Integer requiredMinutes, Integer availableMinutes) {
	}

	private record NeighborKey(Position position, String institutionName, LocalTime startTime, LocalTime endTime,
		int travelMinutes, LocalTime arrivalTime) {

		Neighbor toNeighbor(long count) {
			return new Neighbor(position, institutionName, startTime, endTime, travelMinutes, arrivalTime, count);
		}
	}

	private record Evaluation(Member staff, long classCount, long extraTravelSum, int extraTravelMax,
		List<Neighbor> neighbors, List<Reason> reasons) {
	}
}
