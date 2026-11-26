package com.mongkids.domain.attendance.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mongkids.domain.attendance.entity.Institution;
import com.mongkids.domain.attendance.entity.TravelTime;
import com.mongkids.domain.attendance.repository.TravelTimeRepository;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;
import com.mongkids.global.map.DrivingRouteClient;
import com.mongkids.global.map.DrivingRouteClient.DrivingRoute;
import com.mongkids.global.map.DrivingRouteClient.RouteNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 기관 사이 차량 이동 시간 (EMP-FR-023). 저장된 값이 있으면 그대로 쓰고, 없는 구간만 길찾기 API로 조회해 저장한다.
 * 같은 기관끼리는 조회하지 않고 0분으로 본다.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TravelTimeService {

	private static final int SECONDS_PER_MINUTE = 60;

	private final TravelTimeRepository travelTimeRepository;
	private final DrivingRouteClient drivingRouteClient;

	/**
	 * 구간별 이동 시간(분, 올림). 하나라도 조회하지 못하면 {@code TRAVEL_TIME_UNAVAILABLE}.
	 */
	@Transactional
	public Map<Route, Integer> getMinutes(Collection<Route> routes) {
		Map<Route, Integer> minutes = new HashMap<>();
		Set<Route> missing = new HashSet<>();
		Map<Long, Institution> institutions = new HashMap<>();
		for (Route route : routes) {
			if (route.isSame()) {
				minutes.put(route, 0);
			} else {
				missing.add(route);
				institutions.put(route.from().getId(), route.from());
				institutions.put(route.to().getId(), route.to());
			}
		}
		if (missing.isEmpty()) {
			return minutes;
		}

		for (TravelTime saved : travelTimeRepository.findAmong(institutions.keySet())) {
			Route route = new Route(institutions.get(saved.getFrom().getId()), institutions.get(saved.getTo().getId()));
			if (missing.remove(route)) {
				minutes.put(route, toMinutes(saved.getDurationSeconds()));
			}
		}
		for (Route route : missing) {
			DrivingRoute found = findRoute(route);
			travelTimeRepository.save(TravelTime.builder()
				.from(route.from())
				.to(route.to())
				.durationSeconds(found.durationSeconds())
				.distanceMeters(found.distanceMeters())
				.build());
			minutes.put(route, toMinutes(found.durationSeconds()));
		}
		return minutes;
	}

	/**
	 * 기관 좌표를 바꾸거나 기관을 지울 때 그 기관이 들어간 이동 시간을 지운다.
	 */
	@Transactional
	public void deleteByInstitution(Long institutionId) {
		travelTimeRepository.deleteByInstitutionId(institutionId);
	}

	private DrivingRoute findRoute(Route route) {
		try {
			return drivingRouteClient.findRoute(route.from().getLatitude(), route.from().getLongitude(),
				route.to().getLatitude(), route.to().getLongitude());
		} catch (RouteNotFoundException e) {
			log.warn("이동 시간 조회 실패: from={}, to={}, {}", route.from().getId(), route.to().getId(), e.getMessage());
			throw new BusinessException(ErrorCode.TRAVEL_TIME_UNAVAILABLE);
		}
	}

	static int toMinutes(int seconds) {
		return (seconds + SECONDS_PER_MINUTE - 1) / SECONDS_PER_MINUTE;
	}

	/**
	 * 출발 기관 → 도착 기관. 기관 id로 같은지 비교한다.
	 */
	public record Route(Institution from, Institution to) {

		boolean isSame() {
			return from.getId().equals(to.getId());
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof Route other && from.getId().equals(other.from.getId())
				&& to.getId().equals(other.to.getId());
		}

		@Override
		public int hashCode() {
			return 31 * from.getId().hashCode() + to.getId().hashCode();
		}
	}
}
