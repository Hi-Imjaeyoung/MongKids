package com.mongkids.domain.auth.dto;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import com.mongkids.domain.member.entity.Provider;

class OAuth2UserInfoTest {

	@Test
	@DisplayName("카카오 응답에서 회원번호·닉네임·이메일을 꺼낸다")
	void of_kakao() {
		// given
		Map<String, Object> attributes = Map.of(
			"id", 123456789L,
			"kakao_account", Map.of(
				"profile", Map.of("nickname", "몽키"),
				"email", "monkey@kakao.com"));

		// when
		OAuth2UserInfo userInfo = OAuth2UserInfo.of("kakao", attributes);

		// then
		assertThat(userInfo).isEqualTo(new OAuth2UserInfo(Provider.KAKAO, "123456789", "몽키", "monkey@kakao.com"));
	}

	@Test
	@DisplayName("카카오 닉네임·이메일 동의가 없으면 기본 이름과 null 이메일을 쓴다")
	void of_kakao_withoutConsent() {
		// given
		Map<String, Object> attributes = Map.of("id", 1L);

		// when
		OAuth2UserInfo userInfo = OAuth2UserInfo.of("kakao", attributes);

		// then
		assertThat(userInfo.name()).isEqualTo(OAuth2UserInfo.DEFAULT_NAME);
		assertThat(userInfo.email()).isNull();
	}

	@Test
	@DisplayName("네이버 응답은 response 안의 값을 쓰고, 이름이 없으면 별명을 쓴다")
	void of_naver() {
		// given
		Map<String, Object> attributes = Map.of(
			"resultcode", "00",
			"response", Map.of("id", "naver-id", "nickname", "몽키별명", "email", "monkey@naver.com"));

		// when
		OAuth2UserInfo userInfo = OAuth2UserInfo.of("naver", attributes);

		// then
		assertThat(userInfo).isEqualTo(new OAuth2UserInfo(Provider.NAVER, "naver-id", "몽키별명", "monkey@naver.com"));
	}

	@Test
	@DisplayName("카카오·네이버 휴대폰 번호는 숫자만 국내 형식으로 꺼낸다")
	void of_phone_normalized() {
		// given
		Map<String, Object> kakao = Map.of(
			"id", 1L,
			"kakao_account", Map.of("email", "monkey@kakao.com", "phone_number", "+82 10-1234-5678"));
		Map<String, Object> naver = Map.of(
			"response", Map.of("id", "naver-id", "email", "monkey@naver.com", "mobile", "010-9876-5432"));

		// when & then
		assertThat(OAuth2UserInfo.of("kakao", kakao).phone()).isEqualTo("01012345678");
		assertThat(OAuth2UserInfo.of("naver", naver).phone()).isEqualTo("01098765432");
		assertThat(OAuth2UserInfo.of("naver", naver).toSocialLoginRequest().phone()).isEqualTo("01098765432");
	}

	@Test
	@DisplayName("휴대폰 번호 동의가 없으면 null이고, 국가 번호 없는 번호는 그대로 숫자만 남긴다")
	void normalizePhone() {
		assertThat(OAuth2UserInfo.of("kakao", Map.of("id", 1L)).phone()).isNull();
		assertThat(OAuth2UserInfo.normalizePhone("010 1234 5678")).isEqualTo("01012345678");
		assertThat(OAuth2UserInfo.normalizePhone("+82 2-123-4567")).isEqualTo("021234567");
	}

	@Test
	@DisplayName("이름이 50자를 넘으면 50자로 자른다")
	void of_longName_truncated() {
		// given
		Map<String, Object> attributes = Map.of("response", Map.of("id", "naver-id", "name", "가".repeat(60)));

		// when
		OAuth2UserInfo userInfo = OAuth2UserInfo.of("naver", attributes);

		// then
		assertThat(userInfo.name()).hasSize(50);
	}

	@Test
	@DisplayName("식별자가 없거나 지원하지 않는 서비스면 인증 예외가 발생한다")
	void of_invalid_throwsException() {
		assertThatThrownBy(() -> OAuth2UserInfo.of("naver", Map.of("response", Map.of())))
			.isInstanceOf(OAuth2AuthenticationException.class);
		assertThatThrownBy(() -> OAuth2UserInfo.of("google", Map.of("sub", "1")))
			.isInstanceOf(OAuth2AuthenticationException.class);
	}
}
