package com.mongkids.global.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * 회원 ID(subject)와 역할(role)을 담은 액세스 토큰을 발급·검증한다.
 */
@Component
public class JwtProvider {

	private static final String ROLE_CLAIM = "role";
	private static final String ROLE_PREFIX = "ROLE_";

	private final SecretKey secretKey;
	private final long expirationMillis;

	public JwtProvider(AuthProperties authProperties) {
		this.secretKey = Keys.hmacShaKeyFor(authProperties.jwtSecret().getBytes(StandardCharsets.UTF_8));
		this.expirationMillis = authProperties.tokenExpiration().toMillis();
	}

	public String createToken(Long memberId, String role) {
		Date now = new Date();
		return Jwts.builder()
			.subject(String.valueOf(memberId))
			.claim(ROLE_CLAIM, role)
			.issuedAt(now)
			.expiration(new Date(now.getTime() + expirationMillis))
			.signWith(secretKey)
			.compact();
	}

	/**
	 * 토큰을 검증하고 principal이 회원 ID(Long)인 Authentication을 만든다.
	 *
	 * @throws JwtException 서명·만료·형식이 올바르지 않을 때
	 */
	public Authentication getAuthentication(String token) {
		Claims claims = Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload();

		Long memberId = parseMemberId(claims.getSubject());
		String role = claims.get(ROLE_CLAIM, String.class);
		if (role == null) {
			throw new JwtException("role 클레임이 없습니다.");
		}
		return UsernamePasswordAuthenticationToken.authenticated(
			memberId, null, List.of(new SimpleGrantedAuthority(ROLE_PREFIX + role)));
	}

	public long getExpirationSeconds() {
		return expirationMillis / 1000;
	}

	private Long parseMemberId(String subject) {
		try {
			return Long.valueOf(subject);
		} catch (NumberFormatException e) {
			throw new JwtException("subject가 회원 ID가 아닙니다.", e);
		}
	}
}
