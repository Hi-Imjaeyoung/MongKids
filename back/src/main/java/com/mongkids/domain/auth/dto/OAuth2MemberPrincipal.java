package com.mongkids.domain.auth.dto;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.mongkids.domain.member.dto.LoginMemberResponse;

/**
 * 소셜 로그인 직후의 인증 주체. 로그인 성공 처리에서 회원 ID·역할로 토큰을 발급한다.
 */
public record OAuth2MemberPrincipal(LoginMemberResponse member, Map<String, Object> attributes) implements OAuth2User {

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + member.role().name()));
	}

	@Override
	public String getName() {
		return String.valueOf(member.id());
	}
}
