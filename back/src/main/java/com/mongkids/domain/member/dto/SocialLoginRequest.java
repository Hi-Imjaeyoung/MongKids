package com.mongkids.domain.member.dto;

import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.entity.Provider;

/**
 * 소셜 서비스에서 받은 회원 정보 (FR-018, FR-019). 이메일·휴대폰 번호는 동의하지 않았으면 null이다.
 */
public record SocialLoginRequest(Provider provider, String providerId, String name, String email, String phone) {

	// 휴대폰 번호를 받지 않은 경우
	public SocialLoginRequest(Provider provider, String providerId, String name, String email) {
		this(provider, providerId, name, email, null);
	}

	public Member toEntity() {
		return Member.builder()
			.provider(provider)
			.providerId(providerId)
			.name(name)
			.email(email)
			.phone(phone)
			.build();
	}
}
