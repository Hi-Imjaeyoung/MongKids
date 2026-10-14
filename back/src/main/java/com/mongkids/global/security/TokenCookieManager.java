package com.mongkids.global.security;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 액세스 토큰을 HttpOnly 쿠키로 주고받는다.
 * SameSite=Lax로 다른 사이트에서 보내는 POST 요청에는 쿠키가 실리지 않게 한다 (CSRF 완화).
 */
@Component
@RequiredArgsConstructor
public class TokenCookieManager {

	public static final String COOKIE_NAME = "access_token";

	private final AuthProperties authProperties;

	public void addTokenCookie(HttpServletResponse response, String token, long maxAgeSeconds) {
		response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(token, maxAgeSeconds).toString());
	}

	public void removeTokenCookie(HttpServletResponse response) {
		response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("", 0).toString());
	}

	public Optional<String> resolveToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return Optional.empty();
		}
		return Arrays.stream(cookies)
			.filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
			.map(Cookie::getValue)
			.filter(value -> !value.isBlank())
			.findFirst();
	}

	private ResponseCookie buildCookie(String value, long maxAgeSeconds) {
		return ResponseCookie.from(COOKIE_NAME, value)
			.httpOnly(true)
			.secure(authProperties.cookieSecure())
			.sameSite("Lax")
			.path("/")
			.maxAge(maxAgeSeconds)
			.build();
	}
}
