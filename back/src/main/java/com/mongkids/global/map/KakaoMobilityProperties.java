package com.mongkids.global.map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 카카오모빌리티 길찾기 설정 ({@code app.kakao-mobility.*}).
 *
 * @param restApiKey 카카오 앱 REST API 키. 비어 있으면 길찾기를 호출하지 않고 실패로 본다
 */
@ConfigurationProperties(prefix = "app.kakao-mobility")
public record KakaoMobilityProperties(
	String restApiKey
) {
}
