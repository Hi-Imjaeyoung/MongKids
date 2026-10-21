package com.mongkids.domain.review;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.mongkids.domain.member.dto.LoginMemberResponse;
import com.mongkids.domain.member.dto.SocialLoginRequest;
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
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReviewIntegrationTest {

	private static final String REVIEW_BODY = """
		{"type": "FREE_TRIAL", "satisfaction": 5, "content": "아이가 정말 즐거워했어요",
		 "imageKeys": ["reviews/2026/10/a.jpg"]}
		""";

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

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private FileStorage fileStorage;

	@Test
	@DisplayName("로그인하지 않으면 후기를 쓰거나 사진 업로드 URL을 받을 수 없다")
	void withoutToken_unauthorized() throws Exception {
		mockMvc.perform(post("/api/v1/reviews").contentType(MediaType.APPLICATION_JSON).content(REVIEW_BODY))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("A001"));

		mockMvc.perform(post("/api/v1/reviews/image-upload-urls").contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"files": [{"contentType": "image/jpeg", "size": 1024}]}
					"""))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("만족도가 1~5가 아니거나 본문이 비었거나 1,000자를 넘으면 400과 fieldErrors를 응답한다")
	void createReview_invalidInput_badRequest() throws Exception {
		Cookie member = tokenCookie(registerMember("1", "홍길동"));

		mockMvc.perform(post("/api/v1/reviews").cookie(member).contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"type": "CLASS", "satisfaction": 6, "content": "좋아요"}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("C001"))
			.andExpect(jsonPath("$.error.fieldErrors[0].field").value("satisfaction"));

		mockMvc.perform(post("/api/v1/reviews").cookie(member).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"type", "CLASS", "satisfaction", 3, "content", "가".repeat(1001)))))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.fieldErrors[0].field").value("content"));

		mockMvc.perform(post("/api/v1/reviews").cookie(member).contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"satisfaction": 3, "content": "좋아요"}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.fieldErrors[0].field").value("type"));
	}

	@Test
	@DisplayName("회원이 쓴 후기가 목록에 가린 이름으로 나오고, 다른 회원은 고칠 수 없으며 작성자가 지우면 빠진다")
	void createUpdateDelete() throws Exception {
		// given
		given(fileStorage.findSize(anyString())).willReturn(Optional.of(1024L));
		given(fileStorage.getUrl(anyString())).willAnswer(invocation -> "https://cdn.test/" + invocation.getArgument(0));
		Cookie author = tokenCookie(registerMember("1", "홍길동"));

		// when
		String body = mockMvc.perform(post("/api/v1/reviews").cookie(author)
				.contentType(MediaType.APPLICATION_JSON).content(REVIEW_BODY))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data.authorName").value("홍**"))
			.andExpect(jsonPath("$.data.images[0].imageKey").value("reviews/2026/10/a.jpg"))
			.andExpect(jsonPath("$.data.images[0].url").value("https://cdn.test/reviews/2026/10/a.jpg"))
			.andExpect(jsonPath("$.data.reply").doesNotExist())
			.andReturn().getResponse().getContentAsString();
		long reviewId = objectMapper.readTree(body).path("data").path("id").asLong();

		// then
		mockMvc.perform(get("/api/v1/reviews").param("type", "FREE_TRIAL").cookie(author))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.content[0].id").value(reviewId))
			.andExpect(jsonPath("$.data.content[0].satisfaction").value(5))
			.andExpect(jsonPath("$.data.totalElements").value(1));
		mockMvc.perform(get("/api/v1/reviews").param("type", "CLASS").cookie(author))
			.andExpect(jsonPath("$.data.totalElements").value(0));

		// 다른 회원은 고칠 수 없다
		mockMvc.perform(put("/api/v1/reviews/{reviewId}", reviewId).cookie(tokenCookie(registerMember("2", "김철")))
				.contentType(MediaType.APPLICATION_JSON).content(REVIEW_BODY))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.code").value("A002"));

		mockMvc.perform(put("/api/v1/reviews/{reviewId}", reviewId).cookie(author)
				.contentType(MediaType.APPLICATION_JSON).content("""
					{"type": "CLASS", "satisfaction": 4, "content": "수업 후기로 바꿨어요"}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.type").value("CLASS"))
			.andExpect(jsonPath("$.data.images").isEmpty());

		mockMvc.perform(delete("/api/v1/reviews/{reviewId}", reviewId).cookie(author))
			.andExpect(status().isOk());
		mockMvc.perform(get("/api/v1/reviews").cookie(author))
			.andExpect(jsonPath("$.data.totalElements").value(0));
		mockMvc.perform(delete("/api/v1/reviews/{reviewId}", reviewId).cookie(author))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.code").value("R001"));
	}

	@Test
	@DisplayName("비로그인은 최신 4개만 받고 다음 페이지가 없으며, 숨긴 후기는 누구에게도 보이지 않는다")
	void guestPreview_andHiddenExcluded() throws Exception {
		// given
		LoginMemberResponse writer = registerMember("writer", "몽키");
		for (int i = 1; i <= 6; i++) {
			saveReview(writer, "후기 " + i);
		}
		saveReview(writer, "숨긴 후기").hide();

		// when & then
		mockMvc.perform(get("/api/v1/reviews").param("page", "1").param("size", "2"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.content.length()").value(4))
			.andExpect(jsonPath("$.data.content[0].content").value("후기 6"))
			.andExpect(jsonPath("$.data.totalElements").value(6))
			.andExpect(jsonPath("$.data.hasNext").value(false));

		mockMvc.perform(get("/api/v1/reviews").param("size", "4").cookie(tokenCookie(writer)))
			.andExpect(jsonPath("$.data.content.length()").value(4))
			.andExpect(jsonPath("$.data.totalElements").value(6))
			.andExpect(jsonPath("$.data.hasNext").value(true));
	}

	private Review saveReview(LoginMemberResponse writer, String content) {
		return reviewRepository.save(Review.builder()
			.member(memberRepository.findById(writer.id()).orElseThrow())
			.type(ReviewType.CLASS)
			.satisfaction(5)
			.content(content)
			.build());
	}

	private Cookie tokenCookie(LoginMemberResponse member) {
		return new Cookie(TokenCookieManager.COOKIE_NAME, jwtProvider.createToken(member.id(), member.role().name()));
	}

	private LoginMemberResponse registerMember(String providerId, String name) {
		return memberService.loginOrRegister(new SocialLoginRequest(Provider.KAKAO, providerId, name, null));
	}
}
