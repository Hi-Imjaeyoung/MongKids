package com.mongkids.domain.auth.handler;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.mongkids.domain.auth.dto.OAuth2MemberPrincipal;
import com.mongkids.global.security.AuthProperties;
import com.mongkids.global.security.JwtProvider;
import com.mongkids.global.security.TokenCookieManager;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * 소셜 로그인 성공 시 액세스 토큰을 쿠키로 발급하고 프론트로 돌려보낸다.
 */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

	private final JwtProvider jwtProvider;
	private final TokenCookieManager tokenCookieManager;
	private final AuthProperties authProperties;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		OAuth2MemberPrincipal principal = (OAuth2MemberPrincipal) authentication.getPrincipal();
		String token = jwtProvider.createToken(principal.member().id(), principal.member().role().name());
		tokenCookieManager.addTokenCookie(response, token, jwtProvider.getExpirationSeconds());

		// OAuth2 인가 요청 보관에 쓴 세션은 더 필요 없다
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		response.sendRedirect(authProperties.loginSuccessUrl());
	}
}
