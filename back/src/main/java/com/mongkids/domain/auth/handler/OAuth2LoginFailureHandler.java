package com.mongkids.domain.auth.handler;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.mongkids.global.security.AuthProperties;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 소셜 로그인 실패(사용자 취소, 탈퇴 회원 등) 시 프론트의 실패 주소로 돌려보낸다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

	private final AuthProperties authProperties;
	private final OAuth2WithdrawHandler withdrawHandler;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException exception) throws IOException {
		log.info("소셜 로그인 실패: {}", exception.getMessage());
		// 탈퇴 본인 확인 재로그인 (FR-028)을 취소하거나 실패하면 내 정보 화면으로 돌려보낸다
		if (withdrawHandler.isWithdrawCallback(request)) {
			withdrawHandler.onFailure(response, exception);
			return;
		}
		response.sendRedirect(authProperties.loginFailureUrl());
	}
}
