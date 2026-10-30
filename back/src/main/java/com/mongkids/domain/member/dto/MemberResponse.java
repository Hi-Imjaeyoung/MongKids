package com.mongkids.domain.member.dto;

import java.time.LocalDateTime;

import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.entity.MemberRole;
import com.mongkids.domain.member.entity.Provider;

/**
 * 내 정보 (FR-027). 헤더의 로그인 상태·권한 판단에도 쓴다.
 */
public record MemberResponse(
	Long id,
	Provider provider,
	String name,
	String email,
	String phone,
	boolean snsAgreed,
	LocalDateTime snsAgreedAt,
	MemberRole role,
	LocalDateTime createdAt
) {

	public static MemberResponse from(Member member) {
		return new MemberResponse(
			member.getId(),
			member.getProvider(),
			member.getName(),
			member.getEmail(),
			member.getPhone(),
			member.isSnsAgreed(),
			member.getSnsAgreedAt(),
			member.getRole(),
			member.getCreatedAt()
		);
	}
}
