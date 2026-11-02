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
	ADMIN_ROLE_UNCHANGEABLE(HttpStatus.BAD_REQUEST, "M003", "관리자 권한은 바꿀 수 없습니다."),
	MEMBER_WITHDRAW_NOT_ALLOWED(HttpStatus.FORBIDDEN, "M004", "관리자·직원 계정은 탈퇴할 수 없습니다."),
	WITHDRAW_ACCOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "M005", "가입한 소셜 계정으로 본인 확인을 해 주세요."),
	SOCIAL_UNLINK_FAILED(HttpStatus.BAD_GATEWAY, "M006", "소셜 계정 연결을 끊지 못했습니다. 잠시 후 다시 시도해 주세요."),

	// 상담 신청
	CONSULTATION_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "상담 신청을 찾을 수 없습니다."),

	// 게시물
	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "게시물을 찾을 수 없습니다."),
	POST_IMAGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "P002", "사진은 최대 20장까지 올릴 수 있습니다."),
	INVALID_POST_IMAGE(HttpStatus.BAD_REQUEST, "P003", "올바르지 않은 사진입니다."),
	INVALID_POST_CONTENT(HttpStatus.BAD_REQUEST, "P004", "본문 형식이 올바르지 않습니다."),
	LAB_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "P005", "연구소 글을 찾을 수 없습니다."),
	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "P006", "댓글을 찾을 수 없습니다."),

	// 후기
	REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "후기를 찾을 수 없습니다."),
	REVIEW_IMAGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "R002", "후기 사진은 최대 5장까지 올릴 수 있습니다."),
	INVALID_REVIEW_IMAGE(HttpStatus.BAD_REQUEST, "R003", "올바르지 않은 사진입니다."),

	// 근태 관리
	CLASS_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "E001", "수업을 찾을 수 없습니다."),
	CLASS_START_NOT_ALLOWED(HttpStatus.CONFLICT, "E002", "이미 수업 시작이 확정됐거나 승인 대기 중입니다."),
	CLASS_START_NOT_FOUND(HttpStatus.NOT_FOUND, "E003", "수업 시작 기록이 없습니다."),
	CLASS_START_NOT_PENDING(HttpStatus.CONFLICT, "E004", "승인 대기 중인 수업 시작이 아닙니다."),
	CLASS_END_NOT_ALLOWED(HttpStatus.CONFLICT, "E005", "수업 시작이 확정되거나 승인 대기 중일 때만 수업을 종료할 수 있습니다."),
	CLASS_END_ALREADY_EXISTS(HttpStatus.CONFLICT, "E006", "이미 수업 종료를 제출했습니다."),
	CLASS_END_NOT_FOUND(HttpStatus.NOT_FOUND, "E007", "수업 종료 기록이 없습니다."),
	CLASS_END_CORRECTION_EXPIRED(HttpStatus.CONFLICT, "E008", "수업 기록은 수업 당일까지만 정정할 수 있습니다."),
	CLASS_END_IMAGE_COUNT(HttpStatus.BAD_REQUEST, "E009", "수업 사진은 1장 이상 20장 이하로 올려 주세요."),
	INVALID_CLASS_END_IMAGE(HttpStatus.BAD_REQUEST, "E010", "올바르지 않은 사진입니다."),
	INSTITUTION_NOT_FOUND(HttpStatus.NOT_FOUND, "E011", "기관을 찾을 수 없습니다."),
	INSTITUTION_IN_USE(HttpStatus.CONFLICT, "E012", "할당된 수업이 있는 기관은 삭제할 수 없습니다."),
	CLASS_SESSION_LOCKED(HttpStatus.CONFLICT, "E013", "수업 시작·종료 기록이 있는 수업은 수정하거나 삭제할 수 없습니다."),
	CLASS_SESSION_OVERLAP(HttpStatus.CONFLICT, "E014", "같은 직원의 같은 시간에 이미 수업이 있습니다."),
	INVALID_CLASS_STAFF(HttpStatus.BAD_REQUEST, "E015", "직원 권한이 있는 회원에게만 수업을 할당할 수 있습니다."),
	NO_CLASS_SESSION_CREATED(HttpStatus.BAD_REQUEST, "E016", "기간 안에 고른 요일이 없어 만들 수업이 없습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
