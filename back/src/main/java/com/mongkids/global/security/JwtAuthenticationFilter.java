package com.mongkids.global.security;

import java.io.IOException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 쿠키의 액세스 토큰이 유효하면 SecurityContext에 인증 정보를 넣는다.
 * 토큰이 없거나 유효하지 않으면 비로그인 상태로 다음 필터에 넘긴다.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtProvider jwtProvider;
	private final TokenCookieManager tokenCookieManager;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		tokenCookieManager.resolveToken(request).ifPresent(token -> {
			try {
				SecurityContextHolder.getContext().setAuthentication(jwtProvider.getAuthentication(token));
			} catch (JwtException | IllegalArgumentException e) {
				log.debug("유효하지 않은 액세스 토큰: {}", e.getMessage());
			}
		});
		filterChain.doFilter(request, response);
	}
}
