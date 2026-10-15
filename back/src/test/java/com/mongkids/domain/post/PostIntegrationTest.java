package com.mongkids.domain.post;

import static org.hamcrest.Matchers.startsWith;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.global.security.JwtProvider;
import com.mongkids.global.security.TokenCookieManager;
import com.mongkids.global.storage.FileStorage;

import jakarta.servlet.http.Cookie;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PostIntegrationTest {

	private static final String POST_BODY = """
		{"category": "NEWS", "title": "가을 운동회",
		 "content": "<p>즐거운 <strong>하루</strong></p><script>alert(1)</script><figure class=\\"post-photos\\"><img data-image-key=\\"posts/a.jpg\\"><img data-image-key=\\"posts/b.jpg\\"><figcaption>줄다리기</figcaption></figure>"}
		""";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberService memberService;

	@Autowired
	private JwtProvider jwtProvider;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private FileStorage fileStorage;

	@Test
	@DisplayName("로그인하지 않으면 게시물을 작성할 수 없다")
	void createPost_withoutToken_unauthorized() throws Exception {
		mockMvc.perform(post("/api/v1/posts").contentType(MediaType.APPLICATION_JSON).content(POST_BODY))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("A001"));
	}

	@Test
	@DisplayName("필수값이 없으면 400과 fieldErrors를 응답한다")
	void createPost_invalidInput_badRequest() throws Exception {
		mockMvc.perform(post("/api/v1/posts").cookie(tokenCookie(registerMember("1")))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"category": "NEWS", "title": "", "content": "<p>본문</p>"}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("C001"))
			.andExpect(jsonPath("$.error.fieldErrors[0].field").value("title"));
	}

	@Test
	@DisplayName("작성한 게시물을 목록·상세로 조회하고, 삭제하면 조회되지 않는다")
	void createReadDelete() throws Exception {
		// given
		given(fileStorage.findSize(anyString())).willReturn(Optional.of(1024L));
		given(fileStorage.getUrl(anyString())).willAnswer(invocation -> "https://cdn.test/" + invocation.getArgument(0));
		Cookie writer = tokenCookie(registerMember("1"));

		// when
		String body = mockMvc.perform(post("/api/v1/posts").cookie(writer)
				.contentType(MediaType.APPLICATION_JSON).content(POST_BODY))
			.andExpect(status().isCreated())
			.andReturn().getResponse().getContentAsString();
		long postId = objectMapper.readTree(body).path("data").path("id").asLong();

		// then
		mockMvc.perform(get("/api/v1/posts").param("category", "NEWS"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.content[0].id").value(postId))
			.andExpect(jsonPath("$.data.content[0].thumbnailUrl").value("https://cdn.test/posts/a.jpg"))
			.andExpect(jsonPath("$.data.content[0].authorName").value("몽키"))
			.andExpect(jsonPath("$.data.totalElements").value(1))
			.andExpect(jsonPath("$.data.hasNext").value(false));

		mockMvc.perform(get("/api/v1/posts").param("category", "GENERAL"))
			.andExpect(jsonPath("$.data.totalElements").value(0));

		mockMvc.perform(get("/api/v1/posts/{postId}", postId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.title").value("가을 운동회"))
			.andExpect(jsonPath("$.data.content").value(
				"<p>즐거운 <strong>하루</strong></p><figure class=\"post-photos\">"
					+ "<img data-image-key=\"posts/a.jpg\" src=\"https://cdn.test/posts/a.jpg\" alt=\"\">"
					+ "<img data-image-key=\"posts/b.jpg\" src=\"https://cdn.test/posts/b.jpg\" alt=\"\">"
					+ "<figcaption>줄다리기</figcaption></figure>"));

		// 작성자가 아닌 회원은 삭제할 수 없다
		mockMvc.perform(delete("/api/v1/posts/{postId}", postId).cookie(tokenCookie(registerMember("2"))))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.code").value("A002"));

		mockMvc.perform(delete("/api/v1/posts/{postId}", postId).cookie(writer))
			.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/posts/{postId}", postId))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.code").value("P001"));
	}

	@Test
	@DisplayName("작성자가 게시물을 수정하면 본문과 대표 사진이 바뀐다")
	void updatePost() throws Exception {
		// given
		given(fileStorage.findSize(anyString())).willReturn(Optional.of(1024L));
		given(fileStorage.getUrl(anyString())).willAnswer(invocation -> "https://cdn.test/" + invocation.getArgument(0));
		Cookie writer = tokenCookie(registerMember("1"));
		String body = mockMvc.perform(post("/api/v1/posts").cookie(writer)
				.contentType(MediaType.APPLICATION_JSON).content(POST_BODY))
			.andReturn().getResponse().getContentAsString();
		JsonNode created = objectMapper.readTree(body);
		long postId = created.path("data").path("id").asLong();

		// when
		mockMvc.perform(put("/api/v1/posts/{postId}", postId).cookie(writer)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"category": "EVENT", "title": "수정한 제목",
					 "content": "<p>수정한 본문</p><figure class=\\"post-photos\\"><img data-image-key=\\"posts/b.jpg\\"></figure>"}
					"""))
			.andExpect(status().isOk());

		// then
		mockMvc.perform(get("/api/v1/posts/{postId}", postId))
			.andExpect(jsonPath("$.data.category").value("EVENT"))
			.andExpect(jsonPath("$.data.title").value("수정한 제목"))
			.andExpect(jsonPath("$.data.content").value(startsWith("<p>수정한 본문</p>")));

		mockMvc.perform(get("/api/v1/posts").param("category", "EVENT"))
			.andExpect(jsonPath("$.data.content[0].thumbnailUrl").value("https://cdn.test/posts/b.jpg"));
	}

	@Test
	@DisplayName("본문 형식이 올바르지 않으면 P004를 응답한다")
	void createPost_invalidContent_badRequest() throws Exception {
		mockMvc.perform(post("/api/v1/posts").cookie(tokenCookie(registerMember("1")))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"category": "NEWS", "title": "제목", "content": "<p>글<img data-image-key=\\"posts/a.jpg\\"></p>"}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("P004"));
	}

	private Cookie tokenCookie(LoginMemberResponse member) {
		return new Cookie(TokenCookieManager.COOKIE_NAME, jwtProvider.createToken(member.id(), member.role().name()));
	}

	private LoginMemberResponse registerMember(String providerId) {
		return memberService.loginOrRegister(new SocialLoginRequest(Provider.KAKAO, providerId, "몽키", null));
	}
}
