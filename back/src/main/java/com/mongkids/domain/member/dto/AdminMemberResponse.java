package com.mongkids.domain.member.dto;

import java.time.LocalDateTime;

import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.entity.MemberRole;
import com.mongkids.domain.member.entity.Provider;

/**
 * 관리자 화면의 회원 목록 한 줄 (FR-022).
 */
public record AdminMemberResponse(
	Long id,
	String name,
	String phone,
	Provider provider,
	MemberRole role,
	LocalDateTime createdAt
) {

	public static AdminMemberResponse from(Member member) {
		return new AdminMemberResponse(
			member.getId(),
			member.getName(),
			member.getPhone(),
			member.getProvider(),
			member.getRole(),
			member.getCreatedAt()
		);
	}
}
