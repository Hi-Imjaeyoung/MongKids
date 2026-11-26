package com.mongkids.global.map;

/**
 * 두 좌표 사이 자동차 경로를 찾는다 (EMP-FR-023). 테스트에서는 가짜로 바꿔 끼운다.
 */
public interface DrivingRouteClient {

	/**
	 * 경로를 찾지 못하거나 호출에 실패하면 {@link RouteNotFoundException}을 던진다.
	 */
	DrivingRoute findRoute(double fromLatitude, double fromLongitude, double toLatitude, double toLongitude);

	/**
	 * @param durationSeconds 예상 소요 시간 (초)
	 * @param distanceMeters  거리 (m)
	 */
	record DrivingRoute(int durationSeconds, int distanceMeters) {
	}

	class RouteNotFoundException extends RuntimeException {

		public RouteNotFoundException(String message) {
			super(message);
		}

		public RouteNotFoundException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
