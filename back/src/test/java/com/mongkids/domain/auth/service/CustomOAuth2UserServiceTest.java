package com.mongkids.domain.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import com.mongkids.domain.auth.dto.OAuth2MemberPrincipal;
import com.mongkids.domain.member.dto.LoginMemberResponse;
import com.mongkids.domain.member.dto.SocialLoginRequest;
import com.mongkids.domain.member.entity.MemberRole;
import com.mongkids.domain.member.entity.Provider;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

	private static final Map<String, Object> KAKAO_ATTRIBUTES = Map.of(
		"id", 12345L,
		"kakao_account", Map.of("profile", Map.of("nickname", "몽키")));

	@Mock
	private MemberService memberService;

	@InjectMocks
	private CustomOAuth2UserService customOAuth2UserService;

	@Test
	@DisplayName("소셜 사용자 정보로 회원 로그인 후 회원 ID를 가진 인증 주체를 만든다")
	void toPrincipal_loginMember() {
		// given
		SocialLoginRequest request = new SocialLoginRequest(Provider.KAKAO, "12345", "몽키", null);
		given(memberService.loginOrRegister(request)).willReturn(new LoginMemberResponse(1L, MemberRole.MEMBER));

		// when
		OAuth2MemberPrincipal principal = customOAuth2UserService.toPrincipal("kakao", KAKAO_ATTRIBUTES);

		// then
		assertThat(principal.getName()).isEqualTo("1");
		assertThat(principal.member().role()).isEqualTo(MemberRole.MEMBER);
	}

	@Test
	@DisplayName("탈퇴 회원이면 로그인 실패 처리로 넘어가도록 인증 예외로 바꾼다")
	void toPrincipal_withdrawn_throwsAuthenticationException() {
		// given
		given(memberService.loginOrRegister(any())).willThrow(new BusinessException(ErrorCode.MEMBER_WITHDRAWN));

		// when & then
		assertThatThrownBy(() -> customOAuth2UserService.toPrincipal("kakao", KAKAO_ATTRIBUTES))
			.isInstanceOf(OAuth2AuthenticationException.class)
			.extracting(e -> ((OAuth2AuthenticationException)e).getError().getErrorCode())
			.isEqualTo("M002");
	}
}
