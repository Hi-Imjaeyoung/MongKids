package com.mongkids.domain.member.dto;

import com.mongkids.domain.member.entity.MemberRole;

import jakarta.validation.constraints.NotNull;

/**
 * 직원 권한 부여·회수 요청 (FR-022). {@code STAFF}면 부여, {@code MEMBER}면 회수다.
 */
public record MemberRoleRequest(
	@NotNull MemberRole role
) {
}
