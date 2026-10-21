package com.mongkids.domain.post;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.mongkids.domain.member.dto.LoginMemberResponse;
import com.mongkids.domain.member.dto.SocialLoginRequest;
import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.entity.Provider;
import com.mongkids.domain.member.repository.MemberRepository;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.domain.post.entity.LabCategory;
import com.mongkids.domain.post.entity.LabPost;
import com.mongkids.domain.post.entity.Post;
import com.mongkids.domain.post.entity.PostCategory;
import com.mongkids.domain.post.repository.LabPostRepository;
import com.mongkids.domain.post.repository.PostRepository;
import com.mongkids.global.security.JwtProvider;
import com.mongkids.global.security.TokenCookieManager;
import com.mongkids.global.storage.FileStorage;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PostLikeIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private LabPostRepository labPostRepository;

	@Autowired
	private JwtProvider jwtProvider;

	@MockitoBean
	private FileStorage fileStorage;

	@Test
	@DisplayName("로그인하지 않으면 공감할 수 없지만 공감 수는 볼 수 있다")
	void withoutToken() throws Exception {
		long postId = savePost();

		mockMvc.perform(post("/api/v1/posts/{postId}/likes", postId))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("A001"));

		mockMvc.perform(get("/api/v1/posts/{postId}/likes", postId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.liked").value(false))
			.andExpect(jsonPath("$.data.likeCount").value(0));
	}

	@Test
	@DisplayName("회원당 한 번만 공감되고, 취소하면 공감 수가 돌아온다")
	void likeAndUnlikePost() throws Exception {
		// given
		long postId = savePost();
		Cookie member = tokenCookie(registerMember("1"));

		// when & then
		mockMvc.perform(post("/api/v1/posts/{postId}/likes", postId).cookie(member))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.liked").value(true))
			.andExpect(jsonPath("$.data.likeCount").value(1));

		// 다시 눌러도 한 번만 반영된다
		mockMvc.perform(post("/api/v1/posts/{postId}/likes", postId).cookie(member))
			.andExpect(jsonPath("$.data.likeCount").value(1));

		mockMvc.perform(post("/api/v1/posts/{postId}/likes", postId).cookie(tokenCookie(registerMember("2"))))
			.andExpect(jsonPath("$.data.likeCount").value(2));

		mockMvc.perform(get("/api/v1/posts/{postId}/likes", postId).cookie(member))
			.andExpect(jsonPath("$.data.liked").value(true))
			.andExpect(jsonPath("$.data.likeCount").value(2));
		mockMvc.perform(get("/api/v1/posts/{postId}", postId))
			.andExpect(jsonPath("$.data.likeCount").value(2));

		mockMvc.perform(delete("/api/v1/posts/{postId}/likes", postId).cookie(member))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.liked").value(false))
			.andExpect(jsonPath("$.data.likeCount").value(1));

		mockMvc.perform(delete("/api/v1/posts/{postId}/likes", postId).cookie(member))
			.andExpect(jsonPath("$.data.likeCount").value(1));
	}

	@Test
	@DisplayName("연구소 글도 공감·취소할 수 있다")
	void likeAndUnlikeLabPost() throws Exception {
		// given
		long labPostId = saveLabPost();
		Cookie member = tokenCookie(registerMember("1"));

		// when & then
		mockMvc.perform(post("/api/v1/lab-posts/{labPostId}/likes", labPostId).cookie(member))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.liked").value(true))
			.andExpect(jsonPath("$.data.likeCount").value(1));

		mockMvc.perform(get("/api/v1/lab-posts/{labPostId}", labPostId))
			.andExpect(jsonPath("$.data.likeCount").value(1));

		mockMvc.perform(delete("/api/v1/lab-posts/{labPostId}/likes", labPostId).cookie(member))
			.andExpect(jsonPath("$.data.liked").value(false))
			.andExpect(jsonPath("$.data.likeCount").value(0));
	}

	@Test
	@DisplayName("없는 게시물은 P001, 없는 연구소 글은 P005로 응답한다")
	void notFound() throws Exception {
		mockMvc.perform(get("/api/v1/posts/{postId}/likes", 9999L))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.code").value("P001"));

		mockMvc.perform(post("/api/v1/lab-posts/{labPostId}/likes", 9999L).cookie(tokenCookie(registerMember("1"))))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.code").value("P005"));
	}

	private long savePost() {
		Post post = Post.builder()
			.member(writer())
			.category(PostCategory.GENERAL)
			.title("가을 운동회")
			.content("<p>본문</p>")
			.build();
		return postRepository.save(post).getId();
	}

	private long saveLabPost() {
		LabPost labPost = LabPost.builder()
			.member(writer())
			.category(LabCategory.GROWTH)
			.title("균형 감각 놀이")
			.content("<p>본문</p>")
			.build();
		return labPostRepository.save(labPost).getId();
	}

	private Member writer() {
		return memberRepository.findById(registerMember("writer").id()).orElseThrow();
	}

	private Cookie tokenCookie(LoginMemberResponse member) {
		return new Cookie(TokenCookieManager.COOKIE_NAME, jwtProvider.createToken(member.id(), member.role().name()));
	}

	private LoginMemberResponse registerMember(String providerId) {
		return memberService.loginOrRegister(new SocialLoginRequest(Provider.KAKAO, providerId, "몽키", null));
	}
}
