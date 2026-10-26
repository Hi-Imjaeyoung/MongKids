package com.mongkids.global.exception;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.mongkids.global.common.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
		return toResponse(e.getErrorCode(), ErrorResponse.of(e.getErrorCode()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
		List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
			.map(error -> new ErrorResponse.FieldError(error.getField(), error.getDefaultMessage()))
			.toList();
		return toResponse(ErrorCode.INVALID_INPUT, ErrorResponse.of(ErrorCode.INVALID_INPUT, fieldErrors));
	}

	@ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
	public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception e) {
		return toResponse(ErrorCode.INVALID_INPUT, ErrorResponse.of(ErrorCode.INVALID_INPUT));
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(NoResourceFoundException e) {
		return toResponse(ErrorCode.NOT_FOUND, ErrorResponse.of(ErrorCode.NOT_FOUND));
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
		return toResponse(ErrorCode.METHOD_NOT_ALLOWED, ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED));
	}

	// 응답을 쓰는 중에 클라이언트가 연결을 끊었다 (SSE 탭 닫기·새로고침 등). 보낼 곳이 없으므로 응답 없이 끝낸다
	@ExceptionHandler(AsyncRequestNotUsableException.class)
	public void handleAsyncRequestNotUsable(AsyncRequestNotUsableException e) {
		log.debug("클라이언트 연결 끊김: {}", e.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
		log.error("처리되지 않은 예외", e);
		return toResponse(ErrorCode.INTERNAL_SERVER_ERROR, ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
	}

	// Content-Type을 JSON으로 고정한다. SSE 구독(Accept: text/event-stream)의 권한 오류도 JSON으로 내려간다
	private ResponseEntity<ApiResponse<Void>> toResponse(ErrorCode errorCode, ErrorResponse errorResponse) {
		return ResponseEntity.status(errorCode.getStatus())
			.contentType(MediaType.APPLICATION_JSON)
			.body(ApiResponse.error(errorResponse));
	}
}
