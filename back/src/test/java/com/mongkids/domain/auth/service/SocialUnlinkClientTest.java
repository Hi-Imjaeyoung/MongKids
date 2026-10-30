package com.mongkids.domain.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.mongkids.domain.member.entity.Provider;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;

class SocialUnlinkClientTest {

	private MockRestServiceServer server;
	private SocialUnlinkClient client;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder();
		server = MockRestServiceServer.bindTo(builder).build();
		ClientRegistration naver = ClientRegistration.withRegistrationId(SocialUnlinkClient.NAVER_REGISTRATION_ID)
			.clientId("naver-client-id")
			.clientSecret("naver-client-secret")
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
			.authorizationUri("https://nid.naver.com/oauth2.0/authorize")
			.tokenUri(SocialUnlinkClient.NAVER_TOKEN_URL)
			.build();
		client = new SocialUnlinkClient(builder, new InMemoryClientRegistrationRepository(naver));
	}

	@Test
	@DisplayName("카카오는 액세스 토큰을 Bearer로 보내 연결을 끊는다")
	void unlinkKakao() {
		server.expect(requestTo(SocialUnlinkClient.KAKAO_UNLINK_URL))
			.andExpect(method(HttpMethod.POST))
			.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer kakao-token"))
			.andRespond(withSuccess("{\"id\": 12345}", MediaType.APPLICATION_JSON));

		client.unlink(Provider.KAKAO, "kakao-token");

		server.verify();
	}

	@Test
	@DisplayName("카카오 연결 끊기가 오류로 응답하면 SOCIAL_UNLINK_FAILED 예외가 발생한다")
	void unlinkKakao_error_throwsException() {
		server.expect(requestTo(SocialUnlinkClient.KAKAO_UNLINK_URL)).andRespond(withUnauthorizedRequest());

		assertThatThrownBy(() -> client.unlink(Provider.KAKAO, "expired-token"))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.SOCIAL_UNLINK_FAILED);
	}

	@Test
	@DisplayName("네이버는 앱 키와 액세스 토큰으로 토큰 삭제를 요청한다")
	void unlinkNaver() {
		server.expect(requestTo(SocialUnlinkClient.NAVER_TOKEN_URL))
			.andExpect(method(HttpMethod.POST))
			.andExpect(content().formDataContains(Map.of(
				"grant_type", "delete",
				"client_id", "naver-client-id",
				"client_secret", "naver-client-secret",
				"access_token", "naver-token",
				"service_provider", "NAVER")))
			.andRespond(withSuccess("{\"access_token\": \"naver-token\", \"result\": \"success\"}",
				MediaType.APPLICATION_JSON));

		client.unlink(Provider.NAVER, "naver-token");

		server.verify();
	}

	@Test
	@DisplayName("네이버가 200으로 오류를 돌려주면 SOCIAL_UNLINK_FAILED 예외가 발생한다")
	void unlinkNaver_errorBody_throwsException() {
		server.expect(requestTo(SocialUnlinkClient.NAVER_TOKEN_URL))
			.andRespond(withSuccess("{\"error\": \"invalid_request\"}", MediaType.APPLICATION_JSON));

		assertThatThrownBy(() -> client.unlink(Provider.NAVER, "naver-token"))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.SOCIAL_UNLINK_FAILED);
	}
}
