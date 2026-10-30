package com.mongkids.global.security;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import io.jsonwebtoken.JwtException;

class JwtProviderTest {

	private static final String SECRET = "test-jwt-secret-key-must-be-at-least-32-bytes";

	private final JwtProvider jwtProvider = new JwtProvider(properties(SECRET, Duration.ofHours(1)));

	@Test
	@DisplayName("발급한 토큰을 검증하면 회원 ID와 역할 권한을 가진 인증 정보가 나온다")
	void createToken_thenGetAuthentication() {
		// given
		String token = jwtProvider.createToken(1L, "STAFF");

		// when
		Authentication authentication = jwtProvider.getAuthentication(token);

		// then
		assertThat(authentication.getPrincipal()).isEqualTo(1L);
		assertThat(authentication.getAuthorities())
			.extracting(GrantedAuthority::getAuthority)
			.containsExactly("ROLE_STAFF");
	}

	@Test
	@DisplayName("만료된 토큰은 검증에 실패한다")
	void getAuthentication_expired_throwsException() {
		// given
		JwtProvider expiredProvider = new JwtProvider(properties(SECRET, Duration.ofSeconds(-1)));
		String token = expiredProvider.createToken(1L, "MEMBER");

		// when & then
		assertThatThrownBy(() -> jwtProvider.getAuthentication(token))
			.isInstanceOf(JwtException.class);
	}

	@Test
	@DisplayName("다른 키로 서명한 토큰은 검증에 실패한다")
	void getAuthentication_otherKey_throwsException() {
		// given
		JwtProvider otherProvider = new JwtProvider(
			properties("another-jwt-secret-key-must-be-at-least-32-bytes", Duration.ofHours(1)));
		String token = otherProvider.createToken(1L, "MEMBER");

		// when & then
		assertThatThrownBy(() -> jwtProvider.getAuthentication(token))
			.isInstanceOf(JwtException.class);
	}

	private static AuthProperties properties(String secret, Duration expiration) {
		return new AuthProperties(secret, expiration, false, "/", "/?loginError=true", "/?withdrawn=true", "/me");
	}
}
