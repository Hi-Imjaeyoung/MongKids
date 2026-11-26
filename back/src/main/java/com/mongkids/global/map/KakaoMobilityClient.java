package com.mongkids.global.map;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 카카오모빌리티 자동차 길찾기 (EMP-FR-023). 추천 경로, 실시간 교통은 반영하지 않는다(출발 시각을 주지 않음).
 * https://developers.kakaomobility.com/docs/navi-api/directions/
 */
@Component
@EnableConfigurationProperties(KakaoMobilityProperties.class)
public class KakaoMobilityClient implements DrivingRouteClient {

	static final String DIRECTIONS_URL = "https://apis-navi.kakaomobility.com/v1/directions";
	// 출발지와 도착지가 5m 이내라 경로를 만들지 않음
	static final int RESULT_TOO_CLOSE = 104;
	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
	private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

	private final RestClient restClient;
	private final KakaoMobilityProperties properties;

	public KakaoMobilityClient(KakaoMobilityProperties properties) {
		this.restClient = RestClient.builder().requestFactory(requestFactory()).build();
		this.properties = properties;
	}

	@Override
	public DrivingRoute findRoute(double fromLatitude, double fromLongitude, double toLatitude, double toLongitude) {
		if (!StringUtils.hasText(properties.restApiKey())) {
			throw new RouteNotFoundException("카카오모빌리티 REST API 키가 없습니다");
		}
		DirectionsResponse response;
		try {
			// 좌표는 경도,위도 순서
			response = restClient.get()
				.uri(DIRECTIONS_URL + "?origin={origin}&destination={destination}&priority=RECOMMEND",
					fromLongitude + "," + fromLatitude, toLongitude + "," + toLatitude)
				.header(HttpHeaders.AUTHORIZATION, "KakaoAK " + properties.restApiKey())
				.retrieve()
				.body(DirectionsResponse.class);
		} catch (RestClientException e) {
			throw new RouteNotFoundException("길찾기 호출 실패: " + e.getMessage(), e);
		}
		if (response == null || response.routes() == null || response.routes().isEmpty()) {
			throw new RouteNotFoundException("길찾기 응답에 경로가 없습니다");
		}
		Route route = response.routes().get(0);
		if (route.resultCode() == RESULT_TOO_CLOSE) {
			return new DrivingRoute(0, 0);
		}
		if (route.resultCode() != 0 || route.summary() == null) {
			throw new RouteNotFoundException("길찾기 실패: result_code=" + route.resultCode());
		}
		return new DrivingRoute(route.summary().duration(), route.summary().distance());
	}

	private static SimpleClientHttpRequestFactory requestFactory() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(CONNECT_TIMEOUT);
		factory.setReadTimeout(READ_TIMEOUT);
		return factory;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record DirectionsResponse(List<Route> routes) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Route(@JsonProperty("result_code") int resultCode, Summary summary) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Summary(int distance, int duration) {
	}
}
