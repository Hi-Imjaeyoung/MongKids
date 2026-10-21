package com.mongkids.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// 공통
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다."),
	NOT_FOUND(HttpStatus.NOT_FOUND, "C002", "요청한 리소스를 찾을 수 없습니다."),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C003", "지원하지 않는 HTTP 메서드입니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C999", "서버 오류가 발생했습니다."),

	// 인증·인가
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "로그인이 필요합니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "A002", "권한이 없습니다."),

	// 회원
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "회원을 찾을 수 없습니다."),
	MEMBER_WITHDRAWN(HttpStatus.FORBIDDEN, "M002", "탈퇴한 회원입니다."),

	// 게시물
	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "게시물을 찾을 수 없습니다."),
	POST_IMAGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "P002", "사진은 최대 20장까지 올릴 수 있습니다."),
	INVALID_POST_IMAGE(HttpStatus.BAD_REQUEST, "P003", "올바르지 않은 사진입니다."),
	INVALID_POST_CONTENT(HttpStatus.BAD_REQUEST, "P004", "본문 형식이 올바르지 않습니다."),
	LAB_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "P005", "연구소 글을 찾을 수 없습니다."),
	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "P006", "댓글을 찾을 수 없습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
