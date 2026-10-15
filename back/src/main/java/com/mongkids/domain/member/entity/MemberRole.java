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
}
