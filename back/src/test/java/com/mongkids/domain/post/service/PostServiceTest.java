package com.mongkids.domain.post.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.entity.MemberRole;
import com.mongkids.domain.member.entity.Provider;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.domain.post.dto.PostImageUploadUrlRequest;
import com.mongkids.domain.post.dto.PostImageUploadUrlRequest.ImageFile;
import com.mongkids.domain.post.dto.PostImageUploadUrlResponse;
import com.mongkids.domain.post.dto.PostSaveRequest;
import com.mongkids.domain.post.entity.Post;
import com.mongkids.domain.post.entity.PostCategory;
import com.mongkids.domain.post.entity.PostImage;
import com.mongkids.domain.post.repository.PostRepository;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;
import com.mongkids.global.storage.FileStorage;
import com.mongkids.global.storage.PresignedUpload;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

	private static final Long WRITER_ID = 1L;
	private static final Long OTHER_MEMBER_ID = 2L;
	private static final Long ADMIN_ID = 3L;
	private static final Long POST_ID = 10L;

	@Mock
	private PostRepository postRepository;

	@Mock
	private MemberService memberService;

	@Mock
	private FileStorage fileStorage;

	@Spy
	private PostContentSanitizer contentSanitizer = new PostContentSanitizer();

	private PostService postService;

	@BeforeEach
	void setUp() {
		ArticleWriteSupport writeSupport = new ArticleWriteSupport(memberService, fileStorage, contentSanitizer);
		postService = new PostService(postRepository, writeSupport, fileStorage, contentSanitizer);
	}

	@Test
	@DisplayName("사진 업로드 URL은 posts/ 아래 확장자가 붙은 키로 발급한다")
	void createImageUploadUrls_issuesKeys() {
		// given
		given(memberService.getActiveMember(WRITER_ID)).willReturn(member(WRITER_ID, MemberRole.STAFF));
		given(fileStorage.createUploadUrl(anyString(), eq("image/png"), eq(1024L)))
			.willReturn(new PresignedUpload("https://s3.test/upload", Instant.now()));

		// when
		List<PostImageUploadUrlResponse> responses = postService.createImageUploadUrls(WRITER_ID,
			new PostImageUploadUrlRequest(List.of(new ImageFile("image/png", 1024L))));

		// then
		assertThat(responses).singleElement().satisfies(response -> {
			assertThat(response.imageKey()).startsWith("posts/").endsWith(".png");
			assertThat(response.uploadUrl()).isEqualTo("https://s3.test/upload");
		});
	}

	@Test
	@DisplayName("사진이 20장을 넘으면 업로드 URL을 발급하지 않는다")
	void createImageUploadUrls_overLimit_throwsException() {
		// given
		given(memberService.getActiveMember(WRITER_ID)).willReturn(member(WRITER_ID, MemberRole.STAFF));
		List<ImageFile> files = Collections.nCopies(ArticleWriteSupport.MAX_IMAGE_COUNT + 1, new ImageFile("image/jpeg", 1L));

		// when & then
		assertThatThrownBy(() -> postService.createImageUploadUrls(WRITER_ID, new PostImageUploadUrlRequest(files)))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.POST_IMAGE_LIMIT_EXCEEDED);
	}

	@Test
	@DisplayName("10MB를 넘거나 이미지가 아닌 파일은 업로드 URL을 발급하지 않는다")
	void createImageUploadUrls_invalidFile_throwsException() {
		given(memberService.getActiveMember(WRITER_ID)).willReturn(member(WRITER_ID, MemberRole.STAFF));

		assertThatThrownBy(() -> postService.createImageUploadUrls(WRITER_ID, new PostImageUploadUrlRequest(
			List.of(new ImageFile("image/jpeg", ArticleWriteSupport.MAX_IMAGE_SIZE + 1)))))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.INVALID_POST_IMAGE);

		assertThatThrownBy(() -> postService.createImageUploadUrls(WRITER_ID, new PostImageUploadUrlRequest(
			List.of(new ImageFile("application/pdf", 1024L)))))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.INVALID_POST_IMAGE);
	}

	@Test
	@DisplayName("게시물을 저장하면 본문을 정화하고 본문 속 사진이 나온 순서대로 붙는다")
	void createPost_savesImagesInOrder() {
		// given
		given(memberService.getActiveMember(WRITER_ID)).willReturn(member(WRITER_ID, MemberRole.STAFF));
		given(fileStorage.findSize(anyString())).willReturn(Optional.of(1024L));
		given(postRepository.save(any(Post.class))).willAnswer(invocation -> withId(invocation.getArgument(0)));

		// when
		postService.createPost(WRITER_ID,
			request("<p onclick=\"x()\">글</p>" + photos("posts/a.jpg", "posts/b.jpg")));

		// then
		then(postRepository).should().save(argThat(post ->
			post.getImages().stream().map(PostImage::getImageKey).toList().equals(List.of("posts/a.jpg", "posts/b.jpg"))
				&& post.getImages().get(1).getSortOrder() == 1
				&& !post.getContent().contains("onclick")));
	}

	@Test
	@DisplayName("게시물 사진이 20장을 넘으면 저장하지 않는다")
	void createPost_overImageLimit_throwsException() {
		// given
		given(memberService.getActiveMember(WRITER_ID)).willReturn(member(WRITER_ID, MemberRole.STAFF));
		List<String> imageKeys = IntStream.rangeClosed(0, ArticleWriteSupport.MAX_IMAGE_COUNT)
			.mapToObj(i -> "posts/" + i + ".jpg")
			.toList();

		// when & then
		assertThatThrownBy(() -> postService.createPost(WRITER_ID, request(photos(imageKeys.toArray(String[]::new)))))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.POST_IMAGE_LIMIT_EXCEEDED);
		then(postRepository).should(never()).save(any());
	}

	@Test
	@DisplayName("업로드되지 않았거나 10MB를 넘거나 게시물 경로가 아니거나 중복된 사진 키는 거부한다")
	void createPost_invalidImageKey_throwsException() {
		// given
		given(memberService.getActiveMember(WRITER_ID)).willReturn(member(WRITER_ID, MemberRole.STAFF));
		given(fileStorage.findSize("posts/missing.jpg")).willReturn(Optional.empty());
		given(fileStorage.findSize("posts/large.jpg")).willReturn(Optional.of(ArticleWriteSupport.MAX_IMAGE_SIZE + 1));

		// when & then
		List<List<String>> invalidKeys = List.of(
			List.of("posts/missing.jpg"),
			List.of("posts/large.jpg"),
			List.of("reviews/a.jpg"),
			List.of("posts/a.jpg", "posts/a.jpg"));
		for (List<String> imageKeys : invalidKeys) {
			assertThatThrownBy(() -> postService.createPost(WRITER_ID, request(photos(imageKeys.toArray(String[]::new)))))
				.extracting("errorCode")
				.isEqualTo(ErrorCode.INVALID_POST_IMAGE);
		}
		then(postRepository).should(never()).save(any());
	}

	@Test
	@DisplayName("게시물을 수정하면 사진 목록을 새 본문의 사진으로 바꾼다")
	void updatePost_replacesImages() {
		// given
		given(memberService.getActiveMember(WRITER_ID)).willReturn(member(WRITER_ID, MemberRole.STAFF));
		Post post = post(WRITER_ID, List.of("posts/a.jpg", "posts/b.jpg"));
		given(postRepository.findByIdAndDeletedAtIsNull(POST_ID)).willReturn(Optional.of(post));
		given(fileStorage.findSize(anyString())).willReturn(Optional.of(1024L));

		// when
		postService.updatePost(WRITER_ID, POST_ID, request(photos("posts/c.jpg") + "<p>글</p>" + photos("posts/a.jpg")));

		// then
		assertThat(post.getTitle()).isEqualTo("제목");
		assertThat(post.getImages()).extracting(PostImage::getImageKey, PostImage::getSortOrder)
			.containsExactly(tuple("posts/c.jpg", 0), tuple("posts/a.jpg", 1));
	}

	@Test
	@DisplayName("다른 직원의 게시물은 수정·삭제할 수 없다")
	void updateOrDelete_notWriter_forbidden() {
		// given
		given(memberService.getActiveMember(OTHER_MEMBER_ID)).willReturn(member(OTHER_MEMBER_ID, MemberRole.STAFF));
		Post post = post(WRITER_ID, List.of());
		given(postRepository.findByIdAndDeletedAtIsNull(POST_ID)).willReturn(Optional.of(post));

		// when & then
		assertThatThrownBy(() -> postService.updatePost(OTHER_MEMBER_ID, POST_ID, request("<p>글</p>")))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.FORBIDDEN);
		assertThatThrownBy(() -> postService.deletePost(OTHER_MEMBER_ID, POST_ID))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.FORBIDDEN);
		assertThat(post.isDeleted()).isFalse();
	}

	@Test
	@DisplayName("본문 형식이 올바르지 않으면 저장하지 않는다")
	void createPost_invalidContent_throwsException() {
		// given
		given(memberService.getActiveMember(WRITER_ID)).willReturn(member(WRITER_ID, MemberRole.STAFF));

		// when & then
		assertThatThrownBy(() -> postService.createPost(WRITER_ID, request("<p></p>")))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.INVALID_POST_CONTENT);
		then(postRepository).should(never()).save(any());
	}

	@Test
	@DisplayName("상세 조회는 본문 사진에 URL을 채워 돌려준다")
	void getPost_rendersImageUrls() {
		// given
		Post post = post(WRITER_ID, List.of("posts/a.jpg"));
		given(postRepository.findByIdAndDeletedAtIsNull(POST_ID)).willReturn(Optional.of(post));
		given(fileStorage.getUrl("posts/a.jpg")).willReturn("https://cdn.test/posts/a.jpg");

		// when & then
		assertThat(postService.getPost(POST_ID).content()).contains("src=\"https://cdn.test/posts/a.jpg\"");
	}

	@Test
	@DisplayName("작성자가 삭제하면 soft delete 된다")
	void deletePost_byWriter_softDeletes() {
		// given
		given(memberService.getActiveMember(WRITER_ID)).willReturn(member(WRITER_ID, MemberRole.STAFF));
		Post post = post(WRITER_ID, List.of());
		given(postRepository.findByIdAndDeletedAtIsNull(POST_ID)).willReturn(Optional.of(post));

		// when
		postService.deletePost(WRITER_ID, POST_ID);

		// then
		assertThat(post.isDeleted()).isTrue();
	}

	@Test
	@DisplayName("직원 권한이 없는 회원은 사진 업로드 URL을 받거나 게시물을 작성할 수 없다")
	void createPost_notStaff_forbidden() {
		// given
		given(memberService.getActiveMember(WRITER_ID)).willReturn(member(WRITER_ID, MemberRole.MEMBER));

		// when & then
		assertThatThrownBy(() -> postService.createImageUploadUrls(WRITER_ID,
			new PostImageUploadUrlRequest(List.of(new ImageFile("image/png", 1024L)))))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.FORBIDDEN);
		assertThatThrownBy(() -> postService.createPost(WRITER_ID, request("<p>글</p>")))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.FORBIDDEN);
		then(fileStorage).should(never()).createUploadUrl(anyString(), anyString(), anyLong());
		then(postRepository).should(never()).save(any());
	}

	@Test
	@DisplayName("관리자는 다른 직원의 게시물도 수정·삭제할 수 있다")
	void updateOrDelete_byAdmin_allowed() {
		// given
		given(memberService.getActiveMember(ADMIN_ID)).willReturn(member(ADMIN_ID, MemberRole.ADMIN));
		Post post = post(WRITER_ID, List.of());
		given(postRepository.findByIdAndDeletedAtIsNull(POST_ID)).willReturn(Optional.of(post));

		// when
		postService.updatePost(ADMIN_ID, POST_ID, request("<p>관리자가 고친 글</p>"));
		postService.deletePost(ADMIN_ID, POST_ID);

		// then
		assertThat(post.getContent()).isEqualTo("<p>관리자가 고친 글</p>");
		assertThat(post.isDeleted()).isTrue();
	}

	@Test
	@DisplayName("직원 권한이 회수되면 자기 게시물도 수정·삭제할 수 없다")
	void updateOrDelete_revokedWriter_forbidden() {
		// given
		given(memberService.getActiveMember(WRITER_ID)).willReturn(member(WRITER_ID, MemberRole.MEMBER));

		// when & then
		assertThatThrownBy(() -> postService.updatePost(WRITER_ID, POST_ID, request("<p>글</p>")))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.FORBIDDEN);
		assertThatThrownBy(() -> postService.deletePost(WRITER_ID, POST_ID))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.FORBIDDEN);
		then(postRepository).should(never()).findByIdAndDeletedAtIsNull(any());
	}

	@Test
	@DisplayName("없거나 삭제된 게시물을 조회하면 POST_NOT_FOUND 예외가 발생한다")
	void getPost_notFound_throwsException() {
		// given
		given(postRepository.findByIdAndDeletedAtIsNull(POST_ID)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> postService.getPost(POST_ID))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.POST_NOT_FOUND);
	}

	private static PostSaveRequest request(String content) {
		return new PostSaveRequest(PostCategory.NEWS, "제목", content);
	}

	// 사진을 3장씩 묶은 본문 HTML
	private static String photos(String... imageKeys) {
		StringBuilder html = new StringBuilder();
		for (int i = 0; i < imageKeys.length; i += PostContentSanitizer.MAX_PHOTOS_PER_GROUP) {
			html.append("<figure class=\"post-photos\">");
			for (int j = i; j < Math.min(i + PostContentSanitizer.MAX_PHOTOS_PER_GROUP, imageKeys.length); j++) {
				html.append("<img data-image-key=\"").append(imageKeys[j]).append("\">");
			}
			html.append("</figure>");
		}
		return html.toString();
	}

	private static Member member(Long id, MemberRole role) {
		Member member = Member.builder().provider(Provider.KAKAO).providerId("p" + id).name("몽키").build();
		ReflectionTestUtils.setField(member, "id", id);
		ReflectionTestUtils.setField(member, "role", role);
		return member;
	}

	private static Post post(Long writerId, List<String> imageKeys) {
		Post post = Post.builder()
			.member(member(writerId, MemberRole.STAFF))
			.category(PostCategory.GENERAL)
			.title("원래 제목")
			.content("<p>원래 본문</p>" + photos(imageKeys.toArray(String[]::new)))
			.build();
		imageKeys.forEach(post::addImage);
		return withId(post);
	}

	private static Post withId(Post post) {
		ReflectionTestUtils.setField(post, "id", POST_ID);
		return post;
	}
}
