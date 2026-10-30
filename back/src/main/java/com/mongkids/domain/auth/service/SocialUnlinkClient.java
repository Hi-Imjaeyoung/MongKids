package com.mongkids.domain.auth.service;

import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.mongkids.domain.member.entity.Provider;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

/**
 * 탈퇴 본인 확인에서 받은 액세스 토큰으로 카카오·네이버 연결을 끊는다 (FR-028).
 * 실패하면 {@code SOCIAL_UNLINK_FAILED}를 던지고, 호출한 쪽은 탈퇴를 중단한다.
 */
@Slf4j
@Component
public class SocialUnlinkClient {

	// https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#unlink
	static final String KAKAO_UNLINK_URL = "https://kapi.kakao.com/v1/user/unlink";
	// https://developers.naver.com/docs/login/devguide/devguide.md (접근 토큰 삭제)
	static final String NAVER_TOKEN_URL = "https://nid.naver.com/oauth2.0/token";
	static final String NAVER_REGISTRATION_ID = "naver-withdraw";
	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
	private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

	private final RestClient restClient;
	private final ClientRegistrationRepository clientRegistrationRepository;

	@Autowired
	public SocialUnlinkClient(ClientRegistrationRepository clientRegistrationRepository) {
		this(RestClient.builder().requestFactory(requestFactory()), clientRegistrationRepository);
	}

	SocialUnlinkClient(RestClient.Builder restClientBuilder, ClientRegistrationRepository clientRegistrationRepository) {
		this.restClient = restClientBuilder.build();
		this.clientRegistrationRepository = clientRegistrationRepository;
	}

	public void unlink(Provider provider, String accessToken) {
		try {
			switch (provider) {
				case KAKAO -> unlinkKakao(accessToken);
				case NAVER -> unlinkNaver(accessToken);
			}
		} catch (RestClientException e) {
			log.warn("소셜 연결 끊기 실패: provider={}, {}", provider, e.getMessage());
			throw new BusinessException(ErrorCode.SOCIAL_UNLINK_FAILED);
		}
	}

	private void unlinkKakao(String accessToken) {
		restClient.post()
			.uri(KAKAO_UNLINK_URL)
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
			.retrieve()
			.toBodilessEntity();
	}

	// 네이버는 실패해도 200으로 응답하고 본문의 result가 success가 아니다
	private void unlinkNaver(String accessToken) {
		ClientRegistration registration = clientRegistrationRepository.findByRegistrationId(NAVER_REGISTRATION_ID);
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("grant_type", "delete");
		form.add("client_id", registration.getClientId());
		form.add("client_secret", registration.getClientSecret());
		form.add("access_token", accessToken);
		form.add("service_provider", "NAVER");

		Map<String, Object> body = restClient.post()
			.uri(NAVER_TOKEN_URL)
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body(form)
			.retrieve()
			.body(new ParameterizedTypeReference<>() {
			});
		if (body == null || !"success".equals(body.get("result"))) {
			throw new RestClientException("네이버 토큰 삭제 응답: " + (body == null ? null : body.get("error")));
		}
	}

	private static SimpleClientHttpRequestFactory requestFactory() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(CONNECT_TIMEOUT);
		factory.setReadTimeout(READ_TIMEOUT);
		return factory;
	}
}
