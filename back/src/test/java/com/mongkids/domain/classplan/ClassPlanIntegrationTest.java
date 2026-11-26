package com.mongkids.domain.classplan;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
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

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.mongkids.domain.classplan.entity.ClassPlan;
import com.mongkids.domain.classplan.entity.ClassPlanKind;
import com.mongkids.domain.classplan.repository.ClassPlanRepository;
import com.mongkids.domain.member.dto.LoginMemberResponse;
import com.mongkids.domain.member.dto.SocialLoginRequest;
import com.mongkids.domain.member.entity.MemberRole;
import com.mongkids.domain.member.entity.Provider;
import com.mongkids.domain.member.repository.MemberRepository;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.global.security.JwtProvider;
import com.mongkids.global.security.TokenCookieManager;
import com.mongkids.global.storage.FileStorage;
import com.mongkids.global.storage.PresignedUpload;
import com.mongkids.global.storage.StoredFile;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ClassPlanIntegrationTest {

	private static final String URL = "/api/v1/class-plans";
	private static final String ADMIN_URL = "/api/v1/admin/class-plans";
	private static final String PUBLIC_KEY = "plans/public/2026/11/blur.pdf";
	private static final String ORIGINAL_KEY = "plans/original/2026/11/original.pdf";
	private static final byte[] PUBLIC_PDF = "%PDF-blur".getBytes();
	private static final byte[] ORIGINAL_PDF = "%PDF-original".getBytes();

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private JwtProvider jwtProvider;

	@Autowired
	private ClassPlanRepository classPlanRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private FileStorage fileStorage;

	@BeforeEach
	void setUp() {
		given(fileStorage.findSize(anyString())).willReturn(Optional.of(1024L));
		given(fileStorage.createUploadUrl(anyString(), anyString(), anyLong()))
			.willReturn(new PresignedUpload("https://s3.example/upload", Instant.now()));
		given(fileStorage.open(PUBLIC_KEY))
			.willAnswer(invocation -> Optional.of(new StoredFile(new ByteArrayInputStream(PUBLIC_PDF), PUBLIC_PDF.length)));
		given(fileStorage.open(ORIGINAL_KEY))
			.willAnswer(invocation -> Optional.of(new StoredFile(new ByteArrayInputStream(ORIGINAL_PDF), ORIGINAL_PDF.length)));
	}

	@Test
	@DisplayName("관리자가 아니면 업로드 URL 발급·등록·원본 열람을 할 수 없다")
	void notAdmin_forbidden() throws Exception {
		Cookie member = tokenCookie(registerMember("member", "회원", MemberRole.MEMBER));
		String body = monthlyBody("3월 계획안");

		mockMvc.perform(post(ADMIN_URL).contentType(MediaType.APPLICATION_JSON).content(body))
			.andExpect(status().isUnauthorized());
		mockMvc.perform(post(ADMIN_URL).cookie(member).contentType(MediaType.APPLICATION_JSON).content(body))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.code").value("A002"));
		mockMvc.perform(post(ADMIN_URL + "/file-upload-urls").cookie(member).contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"files": [{"target": "PUBLIC", "contentType": "application/pdf", "size": 1024}]}
					"""))
			.andExpect(status().isForbidden());

		Long id = createPlan(tokenCookie(registerMember("admin", "대표", MemberRole.ADMIN)), body);
		mockMvc.perform(get(ADMIN_URL + "/" + id + "/original-file"))
			.andExpect(status().isUnauthorized());
		mockMvc.perform(get(ADMIN_URL + "/" + id + "/original-file").cookie(member))
			.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("업로드 URL은 PDF만 공개본·원본 경로로 나눠 발급한다")
	void uploadUrls() throws Exception {
		Cookie admin = tokenCookie(registerMember("admin", "대표", MemberRole.ADMIN));

		mockMvc.perform(post(ADMIN_URL + "/file-upload-urls").cookie(admin).contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"files": [
						{"target": "PUBLIC", "contentType": "application/pdf", "size": 1024},
						{"target": "ORIGINAL", "contentType": "application/pdf", "size": 2048}
					]}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].fileKey").value(org.hamcrest.Matchers.startsWith("plans/public/")))
			.andExpect(jsonPath("$.data[1].fileKey").value(org.hamcrest.Matchers.startsWith("plans/original/")))
			.andExpect(jsonPath("$.data[1].uploadUrl").value("https://s3.example/upload"));

		String[] invalid = {
			"""
				{"files": [{"target": "PUBLIC", "contentType": "image/png", "size": 1024}]}
				""",
			"""
				{"files": [{"target": "PUBLIC", "contentType": "application/pdf", "size": %d}]}
				""".formatted(20L * 1024 * 1024 + 1),
		};
		for (String body : invalid) {
			mockMvc.perform(post(ADMIN_URL + "/file-upload-urls").cookie(admin).contentType(MediaType.APPLICATION_JSON)
					.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("L002"));
		}
	}

	@Test
	@DisplayName("연간·월간 계획안을 등록하면 누구나 목록과 블러본을 받고, 원본은 관리자만 받는다. 목록에 파일 키는 없다")
	void create_list_files() throws Exception {
		// given
		Cookie admin = tokenCookie(registerMember("admin", "대표", MemberRole.ADMIN));
		Long monthlyId = createPlan(admin, monthlyBody(" 3월 계획안 "));
		Long annualId = createPlan(admin, annualBody("2027 연간 계획안"));

		// when, then: 연간이 먼저, 월간은 올린 순서
		mockMvc.perform(get(URL))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(2))
			.andExpect(jsonPath("$.data[0].id").value(annualId))
			.andExpect(jsonPath("$.data[0].kind").value("ANNUAL"))
			.andExpect(jsonPath("$.data[0].hasOriginal").value(true))
			.andExpect(jsonPath("$.data[1].id").value(monthlyId))
			.andExpect(jsonPath("$.data[1].title").value("3월 계획안"))
			.andExpect(jsonPath("$.data[1].updatedAt").exists())
			.andExpect(jsonPath("$.data[1].fileKey").doesNotExist())
			.andExpect(jsonPath("$.data[1].originalFileKey").doesNotExist());

		mockMvc.perform(get(URL + "/" + monthlyId + "/file"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_PDF))
			.andExpect(header().string("Content-Disposition", "inline"))
			.andExpect(content().bytes(PUBLIC_PDF));

		mockMvc.perform(get(ADMIN_URL + "/" + monthlyId + "/original-file").cookie(admin))
			.andExpect(status().isOk())
			.andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-store")))
			.andExpect(content().bytes(ORIGINAL_PDF));

		mockMvc.perform(get(ADMIN_URL + "/" + annualId + "/original-file").cookie(admin))
			.andExpect(status().isOk())
			.andExpect(content().bytes(ORIGINAL_PDF));
	}

	@Test
	@DisplayName("연간·월간 모두 원본이 꼭 있어야 하고, 경로가 다르거나 올라가지 않은 파일은 거부한다")
	void create_invalid() throws Exception {
		Cookie admin = tokenCookie(registerMember("admin", "대표", MemberRole.ADMIN));
		given(fileStorage.findSize("plans/public/missing.pdf")).willReturn(Optional.empty());

		String[][] cases = {
			{"""
				{"kind": "MONTHLY", "title": "3월", "period": "2027.03", "fileKey": "%s"}
				""".formatted(PUBLIC_KEY), "L003"},
			{"""
				{"kind": "ANNUAL", "title": "연간", "period": "2027", "fileKey": "%s"}
				""".formatted(PUBLIC_KEY), "L003"},
			{"""
				{"kind": "MONTHLY", "title": "3월", "period": "2027.03", "fileKey": "%s", "originalFileKey": "%s"}
				""".formatted(ORIGINAL_KEY, ORIGINAL_KEY), "L002"},
			{"""
				{"kind": "ANNUAL", "title": "연간", "period": "2027", "fileKey": "plans/public/missing.pdf", "originalFileKey": "%s"}
				""".formatted(ORIGINAL_KEY), "L002"},
			{"""
				{"kind": "ANNUAL", "title": " ", "period": "2027", "fileKey": "%s", "originalFileKey": "%s"}
				""".formatted(PUBLIC_KEY, ORIGINAL_KEY), "C001"},
		};
		for (String[] testCase : cases) {
			mockMvc.perform(post(ADMIN_URL).cookie(admin).contentType(MediaType.APPLICATION_JSON).content(testCase[0]))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value(testCase[1]));
		}
		mockMvc.perform(get(URL)).andExpect(jsonPath("$.data.length()").value(0));
	}

	@Test
	@DisplayName("수정하면 제목·기간과 보낸 파일만 바뀌고, 삭제하면 목록과 파일 주소에서 사라진다")
	void update_delete() throws Exception {
		// given
		Cookie admin = tokenCookie(registerMember("admin", "대표", MemberRole.ADMIN));
		Long id = createPlan(admin, monthlyBody("3월 계획안"));
		String newOriginal = "plans/original/2026/11/new.pdf";
		byte[] newPdf = "%PDF-new".getBytes();
		given(fileStorage.open(newOriginal))
			.willAnswer(invocation -> Optional.of(new StoredFile(new ByteArrayInputStream(newPdf), newPdf.length)));

		// when
		mockMvc.perform(put(ADMIN_URL + "/" + id).cookie(admin).contentType(MediaType.APPLICATION_JSON).content("""
				{"title": "4월 계획안", "period": "2027.04 · 1~4주차", "originalFileKey": "%s"}
				""".formatted(newOriginal)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.title").value("4월 계획안"));

		// then: 블러본은 그대로, 원본만 교체
		mockMvc.perform(get(URL + "/" + id + "/file")).andExpect(content().bytes(PUBLIC_PDF));
		mockMvc.perform(get(ADMIN_URL + "/" + id + "/original-file").cookie(admin)).andExpect(content().bytes(newPdf));

		mockMvc.perform(delete(ADMIN_URL + "/" + id).cookie(admin))
			.andExpect(status().isOk());
		mockMvc.perform(get(URL)).andExpect(jsonPath("$.data.length()").value(0));
		mockMvc.perform(get(URL + "/" + id + "/file"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.code").value("L001"));
	}

	@Test
	@DisplayName("원본 없이 올라간 예전 연간 계획안은 수정으로 원본을 붙인다")
	void update_annualOriginal_attached() throws Exception {
		// given: 2026-11-26 이전처럼 원본 없는 연간 계획안
		Cookie admin = tokenCookie(registerMember("admin", "대표", MemberRole.ADMIN));
		Long id = classPlanRepository.saveAndFlush(ClassPlan.builder()
			.kind(ClassPlanKind.ANNUAL)
			.title("2027 연간 계획안")
			.period("2027")
			.fileKey(PUBLIC_KEY)
			.build()).getId();
		mockMvc.perform(get(URL)).andExpect(jsonPath("$.data[0].hasOriginal").value(false));
		mockMvc.perform(get(ADMIN_URL + "/" + id + "/original-file").cookie(admin))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.code").value("L004"));

		// when
		mockMvc.perform(put(ADMIN_URL + "/" + id).cookie(admin).contentType(MediaType.APPLICATION_JSON).content("""
				{"title": "2027 연간 계획안", "period": "2027", "originalFileKey": "%s"}
				""".formatted(ORIGINAL_KEY)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.hasOriginal").value(true));

		// then
		mockMvc.perform(get(ADMIN_URL + "/" + id + "/original-file").cookie(admin))
			.andExpect(status().isOk())
			.andExpect(content().bytes(ORIGINAL_PDF));
	}

	private static String annualBody(String title) {
		return """
			{"kind": "ANNUAL", "title": "%s", "period": "2027.03 ~ 2028.02", "fileKey": "%s", "originalFileKey": "%s"}
			""".formatted(title, PUBLIC_KEY, ORIGINAL_KEY);
	}

	private static String monthlyBody(String title) {
		return """
			{"kind": "MONTHLY", "title": "%s", "period": "2027.03 · 1~5주차", "fileKey": "%s", "originalFileKey": "%s"}
			""".formatted(title, PUBLIC_KEY, ORIGINAL_KEY);
	}

	private Long createPlan(Cookie admin, String body) throws Exception {
		String response = mockMvc.perform(post(ADMIN_URL).cookie(admin).contentType(MediaType.APPLICATION_JSON).content(body))
			.andExpect(status().isCreated())
			.andReturn().getResponse().getContentAsString();
		JsonNode json = objectMapper.readTree(response);
		return json.path("data").path("id").asLong();
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
