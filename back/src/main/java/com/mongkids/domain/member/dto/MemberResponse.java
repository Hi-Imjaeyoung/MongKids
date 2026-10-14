package com.mongkids.domain.member.dto;

import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.entity.MemberRole;
import com.mongkids.domain.member.entity.Provider;

public record MemberResponse(
	Long id,
	Provider provider,
	String name,
	String email,
	MemberRole role
) {

	public static MemberResponse from(Member member) {
		return new MemberResponse(
			member.getId(),
			member.getProvider(),
			member.getName(),
			member.getEmail(),
			member.getRole()
		);
	}
}
