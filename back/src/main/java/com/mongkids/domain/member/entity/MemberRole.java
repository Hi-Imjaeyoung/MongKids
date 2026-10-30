package com.mongkids.domain.member.entity;

public enum MemberRole {
	MEMBER,
	STAFF,
	ADMIN;

	/**
	 * 게시물을 작성할 수 있는 직원 권한 (FR-009).
	 */
	public boolean canWritePost() {
		return this == STAFF || this == ADMIN;
	}

	public boolean isAdmin() {
		return this == ADMIN;
	}

	/**
	 * 본인이 탈퇴할 수 있는 역할 (FR-028). 직원은 관리자가 직원 해제(FR-022)한 뒤에 탈퇴한다.
	 */
	public boolean canWithdraw() {
		return this == MEMBER;
	}
}
