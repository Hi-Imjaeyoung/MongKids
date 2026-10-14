package com.mongkids.domain.auth.service;

import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.mongkids.domain.auth.dto.OAuth2MemberPrincipal;
import com.mongkids.domain.auth.dto.OAuth2UserInfo;
import com.mongkids.domain.member.dto.LoginMemberResponse;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.global.exception.BusinessException;

import lombok.RequiredArgsConstructor;

/**
 * 소셜 사용자 정보로 회원을 조회하거나 가입시킨다 (FR-018).
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final MemberService memberService;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest);
		return toPrincipal(userRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());
	}

	OAuth2MemberPrincipal toPrincipal(String registrationId, Map<String, Object> attributes) {
		OAuth2UserInfo userInfo = OAuth2UserInfo.of(registrationId, attributes);
		try {
			LoginMemberResponse member = memberService.loginOrRegister(userInfo.toSocialLoginRequest());
			return new OAuth2MemberPrincipal(member, attributes);
		} catch (BusinessException e) {
			throw new OAuth2AuthenticationException(
				new OAuth2Error(e.getErrorCode().getCode(), e.getMessage(), null), e);
		}
	}
}
