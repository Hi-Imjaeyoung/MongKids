package com.mongkids.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import com.mongkids.global.common.ApiResponse;
import com.mongkids.global.security.AuthProperties;
import com.mongkids.global.security.JwtAuthenticationFilter;
import com.mongkids.global.security.JwtProvider;
import com.mongkids.global.security.SecurityResponseWriter;
import com.mongkids.global.security.TokenCookieManager;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 소셜 로그인(OAuth2) + JWT 쿠키 인증 설정.
 * OAuth2 사용자 조회·로그인 성공/실패 처리는 auth 도메인이 빈으로 제공한다.
 */
@Configuration
@EnableConfigurationProperties(AuthProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

	private static final String LOGOUT_URL = "/api/v1/auth/logout";

	private final JwtProvider jwtProvider;
	private final TokenCookieManager tokenCookieManager;
	private final SecurityResponseWriter securityResponseWriter;

	@Bean
	public SecurityFilterChain securityFilterChain(
		HttpSecurity http,
		OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService,
		OAuth2AuthorizationRequestResolver oAuth2AuthorizationRequestResolver,
		AuthenticationSuccessHandler oAuth2LoginSuccessHandler,
		AuthenticationFailureHandler oAuth2LoginFailureHandler
	) throws Exception {
		http
			// 인증 쿠키를 SameSite=Lax로 발급해 교차 사이트 POST에는 쿠키가 실리지 않는다
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			// 로그인 상태는 JWT로만 유지한다 (세션은 OAuth2 인가 요청 보관에만 잠깐 쓰인다)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				// SSE(FR-005)가 끝날 때의 비동기 재디스패치. 처음 요청에서 이미 인증·권한을 확인했고,
				// JWT 필터는 재디스패치에서 돌지 않아 막으면 이미 보낸 응답 위에 401/403을 쓰려다 실패한다
				.dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
				// 내 정보 조회·SNS 수신 동의 변경 (FR-027, FR-019)
				.requestMatchers("/api/v1/members/me", "/api/v1/members/me/**").authenticated()
				// 관리자 화면 API (SCR-014). 관리자인지는 각 Service가 DB의 현재 역할로 검사한다
				.requestMatchers("/api/v1/admin/**").authenticated()
				// 게시물 쓰기 (FR-008). 직원 권한(FR-009)은 PostService가 DB의 현재 역할로 검사한다
				// 댓글 쓰기·삭제(FR-010)와 공감·취소(FR-011)도 여기에 걸린다. 둘 다 로그인한 회원 누구나 쓸 수 있다
				.requestMatchers(HttpMethod.POST, "/api/v1/posts/**").authenticated()
				.requestMatchers(HttpMethod.PUT, "/api/v1/posts/**").authenticated()
				.requestMatchers(HttpMethod.DELETE, "/api/v1/posts/**").authenticated()
				// 몽키즈 연구소 글 쓰기. 권한 규칙은 게시물과 같다 (공감·취소 포함)
				.requestMatchers(HttpMethod.POST, "/api/v1/lab-posts/**").authenticated()
				.requestMatchers(HttpMethod.PUT, "/api/v1/lab-posts/**").authenticated()
				.requestMatchers(HttpMethod.DELETE, "/api/v1/lab-posts/**").authenticated()
				// 후기 쓰기 (FR-013, FR-017, FR-025). 작성은 로그인한 회원 누구나, 수정·삭제는 작성자 본인, 답글은 관리자인지 ReviewService가 검사한다
				.requestMatchers(HttpMethod.POST, "/api/v1/reviews/**").authenticated()
				.requestMatchers(HttpMethod.PUT, "/api/v1/reviews/**").authenticated()
				.requestMatchers(HttpMethod.DELETE, "/api/v1/reviews/**").authenticated()
				// 근태 수업 시작·종료 체크 (EMP-SCR-001). 직원 권한·본인 수업인지는 AttendanceService가 DB로 검사한다
				.requestMatchers("/api/v1/attendance/**").authenticated()
				.anyRequest().permitAll())
			.oauth2Login(oauth2 -> oauth2
				// 탈퇴 본인 확인 재로그인(FR-028)에는 강제 재인증 파라미터를 붙인다
				.authorizationEndpoint(endpoint -> endpoint.authorizationRequestResolver(oAuth2AuthorizationRequestResolver))
				.userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
				.successHandler(oAuth2LoginSuccessHandler)
				.failureHandler(oAuth2LoginFailureHandler))
			.logout(logout -> logout
				.logoutRequestMatcher(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, LOGOUT_URL))
				.addLogoutHandler((request, response, authentication) -> tokenCookieManager.removeTokenCookie(response))
				.logoutSuccessHandler((request, response, authentication) ->
					securityResponseWriter.write(response, HttpServletResponse.SC_OK, ApiResponse.<Void>success(null))))
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint(securityResponseWriter)
				.accessDeniedHandler(securityResponseWriter))
			.addFilterBefore(new JwtAuthenticationFilter(jwtProvider, tokenCookieManager),
				UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}
