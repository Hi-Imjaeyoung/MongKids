package com.mongkids.domain.post;

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
import com.mongkids.domain.member.repository.MemberRepository;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.global.security.JwtProvider;
import com.mongkids.global.security.TokenCookieManager;
import com.mongkids.global.storage.FileStorage;

import jakarta.servlet.http.Cookie;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LabPostIntegrationTest {

	private static final String LAB_POST_BODY = """
		{"category": "GROWTH", "title": "균형 감각 기르기",
		 "content": "<p>한 발 서기 <em>놀이</em></p><figure class=\\"post-photos\\"><img data-image-key=\\"posts/a.jpg\\"></figure>"}
		""";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberService memberService;

	@Autowired
	private JwtProvider jwtProvider;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MemberRepository memberRepository;

	@MockitoBean
	private FileStorage fileStorage;

	@Test
	@DisplayName("로그인하지 않으면 연구소 글을 작성할 수 없다")
	void createLabPost_withoutToken_unauthorized() throws Exception {
		mockMvc.perform(post("/api/v1/lab-posts").contentType(MediaType.APPLICATION_JSON).content(LAB_POST_BODY))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.code").value("A001"));
	}

	@Test
	@DisplayName("직원 권한이 없는 회원은 연구소 글을 작성할 수 없다")
	void createLabPost_notStaff_forbidden() throws Exception {
		mockMvc.perform(post("/api/v1/lab-posts").cookie(tokenCookie(registerMember("1")))
				.contentType(MediaType.APPLICATION_JSON).content(LAB_POST_BODY))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.code").value("A002"));
	}

	@Test
	@DisplayName("수업 현장 분류로는 연구소 글을 작성할 수 없다")
	void createLabPost_postCategory_badRequest() throws Exception {
		mockMvc.perform(post("/api/v1/lab-posts").cookie(tokenCookie(registerStaff("1")))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"category": "NEWS", "title": "제목", "content": "<p>본문</p>"}
					"""))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("작성한 연구소 글은 연구소 목록·상세에만 나오고, 삭제하면 P005로 조회되지 않는다")
	void createReadDelete() throws Exception {
		// given
		given(fileStorage.findSize(anyString())).willReturn(Optional.of(1024L));
		given(fileStorage.getUrl(anyString())).willAnswer(invocation -> "https://cdn.test/" + invocation.getArgument(0));
		Cookie writer = tokenCookie(registerStaff("1"));

		// when
		String body = mockMvc.perform(post("/api/v1/lab-posts").cookie(writer)
				.contentType(MediaType.APPLICATION_JSON).content(LAB_POST_BODY))
			.andExpect(status().isCreated())
			.andReturn().getResponse().getContentAsString();
		long labPostId = objectMapper.readTree(body).path("data").path("id").asLong();

		// then
		mockMvc.perform(get("/api/v1/lab-posts").param("category", "GROWTH"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.content[0].id").value(labPostId))
			.andExpect(jsonPath("$.data.content[0].category").value("GROWTH"))
			.andExpect(jsonPath("$.data.content[0].thumbnailUrl").value("https://cdn.test/posts/a.jpg"))
			.andExpect(jsonPath("$.data.content[0].likeCount").value(0))
			.andExpect(jsonPath("$.data.content[0].commentCount").doesNotExist())
			.andExpect(jsonPath("$.data.totalElements").value(1));

		mockMvc.perform(get("/api/v1/lab-posts").param("category", "PARENT_QA"))
			.andExpect(jsonPath("$.data.totalElements").value(0));

		mockMvc.perform(get("/api/v1/posts"))
			.andExpect(jsonPath("$.data.totalElements").value(0));

		mockMvc.perform(get("/api/v1/lab-posts/{labPostId}", labPostId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.title").value("균형 감각 기르기"))
			.andExpect(jsonPath("$.data.content").value(
				"<p>한 발 서기 <em>놀이</em></p><figure class=\"post-photos\">"
					+ "<img data-image-key=\"posts/a.jpg\" src=\"https://cdn.test/posts/a.jpg\" alt=\"\"></figure>"));

		// 다른 직원은 삭제할 수 없다
		mockMvc.perform(delete("/api/v1/lab-posts/{labPostId}", labPostId).cookie(tokenCookie(registerStaff("2"))))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.code").value("A002"));

		mockMvc.perform(delete("/api/v1/lab-posts/{labPostId}", labPostId).cookie(writer))
			.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/lab-posts/{labPostId}", labPostId))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.code").value("P005"));
	}

	@Test
	@DisplayName("작성자가 연구소 글을 수정하면 분류와 제목이 바뀐다")
	void updateLabPost() throws Exception {
		// given
		given(fileStorage.findSize(anyString())).willReturn(Optional.of(1024L));
		Cookie writer = tokenCookie(registerStaff("1"));
		String body = mockMvc.perform(post("/api/v1/lab-posts").cookie(writer)
				.contentType(MediaType.APPLICATION_JSON).content(LAB_POST_BODY))
			.andReturn().getResponse().getContentAsString();
		long labPostId = objectMapper.readTree(body).path("data").path("id").asLong();

		// when
		mockMvc.perform(put("/api/v1/lab-posts/{labPostId}", labPostId).cookie(writer)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"category": "EQUIPMENT", "title": "밸런스 보드 이야기", "content": "<p>수정한 본문</p>"}
					"""))
			.andExpect(status().isOk());

		// then
		mockMvc.perform(get("/api/v1/lab-posts/{labPostId}", labPostId))
			.andExpect(jsonPath("$.data.category").value("EQUIPMENT"))
			.andExpect(jsonPath("$.data.title").value("밸런스 보드 이야기"))
			.andExpect(jsonPath("$.data.content").value("<p>수정한 본문</p>"));
	}

	private Cookie tokenCookie(LoginMemberResponse member) {
		return new Cookie(TokenCookieManager.COOKIE_NAME, jwtProvider.createToken(member.id(), member.role().name()));
	}

	// 직원 권한 부여(FR-022) API가 없어서 테스트에서는 회원 Entity로 직접 부여한다
	private LoginMemberResponse registerStaff(String providerId) {
		LoginMemberResponse member = registerMember(providerId);
		memberRepository.findById(member.id()).orElseThrow().grantStaff();
		return member;
	}

	private LoginMemberResponse registerMember(String providerId) {
		return memberService.loginOrRegister(new SocialLoginRequest(Provider.KAKAO, providerId, "몽키", null));
	}
}
