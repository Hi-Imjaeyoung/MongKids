package com.mongkids.global.security;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.mongkids.global.common.ApiResponse;
import com.mongkids.global.exception.ErrorCode;
import com.mongkids.global.exception.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

/**
 * 시큐리티 필터 단계의 응답(401·403, 로그아웃 성공)을 {@link ApiResponse} 형식으로 쓴다.
 */
@Component
@RequiredArgsConstructor
public class SecurityResponseWriter implements AuthenticationEntryPoint, AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException authException) throws IOException {
		write(response, ErrorCode.UNAUTHORIZED.getStatus().value(),
			ApiResponse.error(ErrorResponse.of(ErrorCode.UNAUTHORIZED)));
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
		AccessDeniedException accessDeniedException) throws IOException {
		write(response, ErrorCode.FORBIDDEN.getStatus().value(),
			ApiResponse.error(ErrorResponse.of(ErrorCode.FORBIDDEN)));
	}

	public void write(HttpServletResponse response, int status, ApiResponse<?> body) throws IOException {
		response.setStatus(status);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(objectMapper.writeValueAsString(body));
	}
}
