package com.mongkids.global.security;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 인증 설정 ({@code app.auth.*}).
 *
 * @param jwtSecret       JWT 서명 키 (HS256, 32바이트 이상). 환경변수로만 주입한다.
 * @param tokenExpiration 액세스 토큰 유효 기간
 * @param cookieSecure    인증 쿠키 Secure 속성 (HTTPS 환경에서 true)
 * @param loginSuccessUrl 소셜 로그인 성공 후 이동할 프론트 주소
 * @param loginFailureUrl 소셜 로그인 실패 후 이동할 프론트 주소
 */
@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
	String jwtSecret,
	Duration tokenExpiration,
	boolean cookieSecure,
	String loginSuccessUrl,
	String loginFailureUrl
) {
}
