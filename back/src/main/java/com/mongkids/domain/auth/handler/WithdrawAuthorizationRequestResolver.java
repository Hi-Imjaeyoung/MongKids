package com.mongkids.domain.auth.handler;

import java.util.Map;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Component;

import com.mongkids.domain.auth.dto.OAuth2UserInfo;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 탈퇴 본인 확인 재로그인(FR-028)의 인가 요청에 강제 재인증 파라미터를 붙인다.
 * 이미 소셜 서비스에 로그인해 있어도 계정 입력을 다시 받게 한다. 일반 로그인 요청은 그대로 둔다.
 */
@Component
public class WithdrawAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

	// https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#request-code (prompt=login)
	// https://developers.naver.com/docs/login/devguide/devguide.md (auth_type=reauthenticate)
	private static final Map<String, Map<String, Object>> REAUTHENTICATE_PARAMETERS = Map.of(
		"kakao" + OAuth2UserInfo.WITHDRAW_SUFFIX, Map.of("prompt", "login"),
		"naver" + OAuth2UserInfo.WITHDRAW_SUFFIX, Map.of("auth_type", "reauthenticate"));

	private final DefaultOAuth2AuthorizationRequestResolver delegate;

	public WithdrawAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
		this.delegate = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository,
			OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);
		this.delegate.setAuthorizationRequestCustomizer(builder -> builder.attributes(attributes -> {
			Object registrationId = attributes.get(OAuth2ParameterNames.REGISTRATION_ID);
			Map<String, Object> parameters = REAUTHENTICATE_PARAMETERS.get(registrationId);
			if (parameters != null) {
				builder.additionalParameters(additional -> additional.putAll(parameters));
			}
		}));
	}

	@Override
	public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
		return delegate.resolve(request);
	}

	@Override
	public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
		return delegate.resolve(request, clientRegistrationId);
	}
}
