package com.mongkids.domain.auth.handler;

import java.io.IOException;
import java.util.Optional;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.mongkids.domain.auth.dto.OAuth2UserInfo;
import com.mongkids.domain.auth.dto.OAuth2WithdrawPrincipal;
import com.mongkids.domain.auth.service.SocialWithdrawService;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;
import com.mongkids.global.security.AuthProperties;
import com.mongkids.global.security.JwtProvider;
import com.mongkids.global.security.TokenCookieManager;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 탈퇴 본인 확인 재로그인(FR-028)이 끝난 뒤의 처리. 로그인 성공·실패 처리에서 넘겨받는다.
 * 결과는 프론트 내 정보 화면으로 돌려보내고, 실패하면 {@code withdrawError}에 코드를 붙인다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2WithdrawHandler {

	static final String ERROR_PARAM = "withdrawError";
	// 사용자가 소셜 로그인 화면에서 취소했을 때
	static final String CANCELLED = "cancelled";
	static final String FAILED = "failed";

	private final SocialWithdrawService socialWithdrawService;
	private final OAuth2AuthorizedClientService authorizedClientService;
	private final JwtProvider jwtProvider;
	private final TokenCookieManager tokenCookieManager;
	private final AuthProperties authProperties;

	public boolean isWithdrawCallback(HttpServletRequest request) {
		return OAuth2UserInfo.isWithdrawRegistration(request.getRequestURI());
	}

	public void onSuccess(HttpServletRequest request, HttpServletResponse response,
		OAuth2WithdrawPrincipal principal) throws IOException {
		// OAuth2 로그인 필터가 메모리에 보관해 둔 소셜 토큰을 바로 지운다 (토큰은 저장하지 않는다)
		authorizedClientService.removeAuthorizedClient(principal.registrationId(), principal.getName());

		Optional<Long> memberId = resolveMemberId(request);
		if (memberId.isEmpty()) {
			redirectFailure(response, ErrorCode.UNAUTHORIZED.getCode());
			return;
		}
		try {
			socialWithdrawService.withdraw(memberId.get(), principal);
		} catch (BusinessException e) {
			log.info("회원 탈퇴 실패: memberId={}, {}", memberId.get(), e.getErrorCode().getCode());
			redirectFailure(response, e.getErrorCode().getCode());
			return;
		} catch (RuntimeException e) {
			// 브라우저 이동 중이라 오류 페이지 대신 내 정보 화면으로 돌려보낸다. 트랜잭션은 이미 롤백됐다
			log.error("회원 탈퇴 중 오류: memberId={}", memberId.get(), e);
			redirectFailure(response, FAILED);
			return;
		}
		tokenCookieManager.removeTokenCookie(response);
		response.sendRedirect(authProperties.withdrawSuccessUrl());
	}

	public void onFailure(HttpServletResponse response, AuthenticationException exception) throws IOException {
		boolean cancelled = exception instanceof OAuth2AuthenticationException oAuth2Exception
			&& OAuth2ErrorCodes.ACCESS_DENIED.equals(oAuth2Exception.getError().getErrorCode());
		redirectFailure(response, cancelled ? CANCELLED : FAILED);
	}

	// 탈퇴할 회원은 재로그인 전부터 가지고 있던 로그인 쿠키로 정한다
	private Optional<Long> resolveMemberId(HttpServletRequest request) {
		return tokenCookieManager.resolveToken(request).flatMap(token -> {
			try {
				return Optional.of((Long) jwtProvider.getAuthentication(token).getPrincipal());
			} catch (JwtException | IllegalArgumentException e) {
				return Optional.empty();
			}
		});
	}

	private void redirectFailure(HttpServletResponse response, String code) throws IOException {
		response.sendRedirect(UriComponentsBuilder.fromUriString(authProperties.withdrawFailureUrl())
			.queryParam(ERROR_PARAM, code)
			.build()
			.toUriString());
	}
}
