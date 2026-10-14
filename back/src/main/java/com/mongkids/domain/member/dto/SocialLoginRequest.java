package com.mongkids.domain.member.dto;

import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.entity.Provider;

/**
 * 소셜 서비스에서 받은 회원 정보 (FR-018).
 */
public record SocialLoginRequest(Provider provider, String providerId, String name, String email) {

	public Member toEntity() {
		return Member.builder()
			.provider(provider)
			.providerId(providerId)
			.name(name)
			.email(email)
			.build();
	}
}
