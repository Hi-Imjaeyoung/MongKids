package com.mongkids.domain.member.dto;

import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.entity.MemberRole;

/**
 * 토큰 발급에 필요한 로그인 회원 정보.
 */
public record LoginMemberResponse(Long id, MemberRole role) {

	public static LoginMemberResponse from(Member member) {
		return new LoginMemberResponse(member.getId(), member.getRole());
	}
}
