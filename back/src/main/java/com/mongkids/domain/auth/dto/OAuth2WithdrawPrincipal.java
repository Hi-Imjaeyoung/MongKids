package com.mongkids.domain.auth.dto;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.mongkids.domain.member.entity.Provider;

/**
 * 탈퇴 본인 확인으로 다시 로그인한 소셜 계정 (FR-028). 회원을 조회·가입시키지 않는다.
 * 액세스 토큰은 연결 끊기에 한 번 쓰고 저장하지 않는다.
 */
public record OAuth2WithdrawPrincipal(
	String registrationId,
	Provider provider,
	String providerId,
	String accessToken,
	Map<String, Object> attributes
) implements OAuth2User {

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of();
	}

	@Override
	public String getName() {
		return providerId;
	}

	// 로그에 토큰이 찍히지 않게 한다
	@Override
	public String toString() {
		return "OAuth2WithdrawPrincipal[registrationId=" + registrationId + ", provider=" + provider + "]";
	}
}
