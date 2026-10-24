package com.mongkids.domain.review;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.mongkids.domain.member.dto.LoginMemberResponse;
import com.mongkids.domain.member.dto.SocialLoginRequest;
import com.mongkids.domain.member.entity.MemberRole;
import com.mongkids.domain.member.entity.Provider;
import com.mongkids.domain.member.repository.MemberRepository;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.domain.review.entity.Review;
import com.mongkids.domain.review.entity.ReviewType;
import com.mongkids.domain.review.repository.ReviewRepository;
import com.mongkids.global.security.JwtProvider;
import com.mongkids.global.security.TokenCookieManager;
import com.mongkids.global.storage.FileStorage;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminReviewIntegrationTest {

	private static final String URL = "/api/v1/admin/reviews";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private ReviewRepository reviewRepository;

	@Autowired
	private JwtProvider jwtProvider;

	@MockitoBean
	private FileStorage fileStorage;

	@Test
	@DisplayName("관리자가 아니면 관리자 후기 목록을 보거나 후기를 숨길 수 없다")
	void notAdmin_forbidden() throws Exception {
		LoginMemberResponse writer = registerMember("writer", "홍길동", MemberRole.MEMBER);
		long reviewId = saveReview(writer, ReviewType.CLASS, "좋았어요").getId();

		mockMvc.perform(get(URL).cookie(tokenCookie(writer)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.code").value("A002"));

		mockMvc.perform(put(URL + "/{id}/hidden", reviewId).cookie(tokenCookie(writer))
				.contentType(MediaType.APPLICATION_JSON).content("""
					{"hidden": true}
					"""))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("관리자가 숨긴 후기는 공개 목록에서 빠지고 관리자 목록에는 숨김으로 남으며, 다시 공개할 수 있다")
	void hideAndUnhide() throws Exception {
		// given
		Cookie admin = tokenCookie(registerMember("admin", "대표", MemberRole.ADMIN));
		LoginMemberResponse writer = registerMember("writer", "홍길동", MemberRole.MEMBER);
		long hiddenId = saveReview(writer, ReviewType.CLASS, "광고 문구").getId();
		saveReview(writer, ReviewType.FREE_TRIAL, "좋았어요");

		// when
		mockMvc.perform(put(URL + "/{id}/hidden", hiddenId).cookie(admin)
				.contentType(MediaType.APPLICATION_JSON).content("""
					{"hidden": true}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.hidden").value(true))
			.andExpect(jsonPath("$.data.hiddenAt").exists())
			.andExpect(jsonPath("$.data.review.authorName").value("홍**"));

		// then
		mockMvc.perform(get("/api/v1/reviews"))
			.andExpect(jsonPath("$.data.totalElements").value(1));

		mockMvc.perform(get(URL).cookie(admin))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.totalElements").value(2));

		mockMvc.perform(get(URL).param("hiddenOnly", "true").cookie(admin))
			.andExpect(jsonPath("$.data.totalElements").value(1))
			.andExpect(jsonPath("$.data.content[0].review.id").value(hiddenId))
			.andExpect(jsonPath("$.data.content[0].hidden").value(true));

		mockMvc.perform(get(URL).param("type", "FREE_TRIAL").cookie(admin))
			.andExpect(jsonPath("$.data.totalElements").value(1))
			.andExpect(jsonPath("$.data.content[0].hidden").value(false));

		mockMvc.perform(put(URL + "/{id}/hidden", hiddenId).cookie(admin)
				.contentType(MediaType.APPLICATION_JSON).content("""
					{"hidden": false}
					"""))
			.andExpect(jsonPath("$.data.hidden").value(false))
			.andExpect(jsonPath("$.data.hiddenAt").doesNotExist());

		mockMvc.perform(get("/api/v1/reviews"))
			.andExpect(jsonPath("$.data.totalElements").value(2));

		mockMvc.perform(put(URL + "/{id}/hidden", 9999L).cookie(admin)
				.contentType(MediaType.APPLICATION_JSON).content("""
					{"hidden": true}
					"""))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.code").value("R001"));
	}

	private Review saveReview(LoginMemberResponse writer, ReviewType type, String content) {
		return reviewRepository.save(Review.builder()
			.member(memberRepository.findById(writer.id()).orElseThrow())
			.type(type)
			.satisfaction(5)
			.content(content)
			.build());
	}

	private Cookie tokenCookie(LoginMemberResponse member) {
		return new Cookie(TokenCookieManager.COOKIE_NAME, jwtProvider.createToken(member.id(), member.role().name()));
	}

	private LoginMemberResponse registerMember(String providerId, String name, MemberRole role) {
		LoginMemberResponse member = memberService.loginOrRegister(
			new SocialLoginRequest(Provider.KAKAO, providerId, name, null));
		ReflectionTestUtils.setField(memberRepository.findById(member.id()).orElseThrow(), "role", role);
		return member;
	}
}
