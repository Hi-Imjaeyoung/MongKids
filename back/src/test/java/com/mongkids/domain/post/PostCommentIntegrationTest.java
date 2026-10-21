package com.mongkids.domain.post;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

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
import com.mongkids.domain.post.entity.Post;
import com.mongkids.domain.post.entity.PostCategory;
import com.mongkids.domain.post.repository.PostRepository;
import com.mongkids.global.security.JwtProvider;
import com.mongkids.global.security.TokenCookieManager;
import com.mongkids.global.storage.FileStorage;

import jakarta.servlet.http.Cookie;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PostCommentIntegrationTest {

	private static final String COMMENT_BODY = """
		{"content": "아이가 정말 즐거워했어요"}
		""";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private JwtProvider jwtProvider;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private FileStorage fileStorage;

	@Test
	@DisplayName("로그인하지 않으면 댓글을 쓸 수 없지만 목록은 볼 수 있다")
	void withoutToken() throws Exception {
		long postId = savePost();

		mockMvc.perform(post("/api/v1/posts/{postId}/comments", postId)
				.contentType(MediaType.APPLICATION_JSON).content(COMMENT_BODY))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("A001"));

		mockMvc.perform(get("/api/v1/posts/{postId}/comments", postId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.totalElements").value(0));
	}

	@Test
	@DisplayName("본문이 비었거나 1,000자를 넘으면 400과 fieldErrors를 응답한다")
	void createComment_invalidInput_badRequest() throws Exception {
		long postId = savePost();
		Cookie member = tokenCookie(registerMember("1"));

		mockMvc.perform(post("/api/v1/posts/{postId}/comments", postId).cookie(member)
				.contentType(MediaType.APPLICATION_JSON).content("""
					{"content": " "}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.code").value("C001"))
			.andExpect(jsonPath("$.error.fieldErrors[0].field").value("content"));

		mockMvc.perform(post("/api/v1/posts/{postId}/comments", postId).cookie(member)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("content", "가".repeat(1001)))))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.fieldErrors[0].field").value("content"));
	}

	@Test
	@DisplayName("회원이 쓴 댓글이 목록과 댓글 수에 반영되고, 작성자가 지우면 빠진다")
	void createReadDelete() throws Exception {
		// given
		long postId = savePost();
		Cookie author = tokenCookie(registerMember("1"));

		// when
		String body = mockMvc.perform(post("/api/v1/posts/{postId}/comments", postId).cookie(author)
				.contentType(MediaType.APPLICATION_JSON).content(COMMENT_BODY))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data.content").value("아이가 정말 즐거워했어요"))
			.andExpect(jsonPath("$.data.authorName").value("몽키"))
			.andReturn().getResponse().getContentAsString();
		long commentId = objectMapper.readTree(body).path("data").path("id").asLong();

		// then
		mockMvc.perform(get("/api/v1/posts/{postId}/comments", postId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.content[0].id").value(commentId))
			.andExpect(jsonPath("$.data.totalElements").value(1));
		mockMvc.perform(get("/api/v1/posts/{postId}", postId))
			.andExpect(jsonPath("$.data.commentCount").value(1));

		// 다른 회원은 지울 수 없다
		mockMvc.perform(delete("/api/v1/posts/{postId}/comments/{commentId}", postId, commentId)
				.cookie(tokenCookie(registerMember("2"))))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.code").value("A002"));

		mockMvc.perform(delete("/api/v1/posts/{postId}/comments/{commentId}", postId, commentId).cookie(author))
			.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/posts/{postId}/comments", postId))
			.andExpect(jsonPath("$.data.totalElements").value(0));
		mockMvc.perform(get("/api/v1/posts/{postId}", postId))
			.andExpect(jsonPath("$.data.commentCount").value(0));

		mockMvc.perform(delete("/api/v1/posts/{postId}/comments/{commentId}", postId, commentId).cookie(author))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.code").value("P006"));
	}

	@Test
	@DisplayName("없는 게시물의 댓글은 P001로 응답한다")
	void postNotFound() throws Exception {
		mockMvc.perform(get("/api/v1/posts/{postId}/comments", 9999L))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.code").value("P001"));
	}

	private long savePost() {
		LoginMemberResponse writer = registerMember("writer");
		Post post = Post.builder()
			.member(memberRepository.findById(writer.id()).orElseThrow())
			.category(PostCategory.GENERAL)
			.title("가을 운동회")
			.content("<p>본문</p>")
			.build();
		return postRepository.save(post).getId();
	}

	private Cookie tokenCookie(LoginMemberResponse member) {
		return new Cookie(TokenCookieManager.COOKIE_NAME, jwtProvider.createToken(member.id(), member.role().name()));
	}

	private LoginMemberResponse registerMember(String providerId) {
		return memberService.loginOrRegister(new SocialLoginRequest(Provider.KAKAO, providerId, "몽키", null));
	}
}
