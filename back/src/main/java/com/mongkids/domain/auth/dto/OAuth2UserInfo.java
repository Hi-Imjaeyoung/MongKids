package com.mongkids.domain.auth.dto;

import java.util.Map;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import com.mongkids.domain.member.dto.SocialLoginRequest;
import com.mongkids.domain.member.entity.Provider;

/**
 * 카카오·네이버 사용자 정보 응답에서 회원 가입에 필요한 값만 꺼낸다.
 * 이름이 없으면 기본 이름을 쓰고, 이메일은 동의하지 않았으면 null이다.
 */
public record OAuth2UserInfo(Provider provider, String providerId, String name, String email) {

	public static final String DEFAULT_NAME = "회원";
	private static final int NAME_MAX_LENGTH = 50;

	public static OAuth2UserInfo of(String registrationId, Map<String, Object> attributes) {
		return switch (registrationId) {
			case "kakao" -> ofKakao(attributes);
			case "naver" -> ofNaver(attributes);
			default -> throw new OAuth2AuthenticationException(
				new OAuth2Error("unsupported_provider", "지원하지 않는 소셜 로그인입니다: " + registrationId, null));
		};
	}

	// https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#req-user-info
	private static OAuth2UserInfo ofKakao(Map<String, Object> attributes) {
		Map<String, Object> account = getMap(attributes, "kakao_account");
		Map<String, Object> profile = getMap(account, "profile");
		return new OAuth2UserInfo(
			Provider.KAKAO,
			requireId(attributes.get("id")),
			normalizeName(getString(profile, "nickname")),
			getString(account, "email")
		);
	}

	// https://developers.naver.com/docs/login/profile/profile.md
	private static OAuth2UserInfo ofNaver(Map<String, Object> attributes) {
		Map<String, Object> response = getMap(attributes, "response");
		String name = getString(response, "name");
		return new OAuth2UserInfo(
			Provider.NAVER,
			requireId(response.get("id")),
			normalizeName(name != null ? name : getString(response, "nickname")),
			getString(response, "email")
		);
	}

	public SocialLoginRequest toSocialLoginRequest() {
		return new SocialLoginRequest(provider, providerId, name, email);
	}

	private static String requireId(Object id) {
		if (id == null || String.valueOf(id).isBlank()) {
			throw new OAuth2AuthenticationException(
				new OAuth2Error("invalid_user_info", "소셜 사용자 식별자가 없습니다.", null));
		}
		return String.valueOf(id);
	}

	private static String normalizeName(String name) {
		if (name == null || name.isBlank()) {
			return DEFAULT_NAME;
		}
		String trimmed = name.strip();
		return trimmed.length() > NAME_MAX_LENGTH ? trimmed.substring(0, NAME_MAX_LENGTH) : trimmed;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> getMap(Map<String, Object> source, String key) {
		Object value = source.get(key);
		return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
	}

	private static String getString(Map<String, Object> source, String key) {
		Object value = source.get(key);
		return value instanceof String string && !string.isBlank() ? string : null;
	}
}
