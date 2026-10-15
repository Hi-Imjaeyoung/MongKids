package com.mongkids.domain.post.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.mongkids.domain.member.entity.Member;
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
	private static final Long POST_ID = 10L;

	@Mock
	private PostRepository postRepository;

	@Mock
	private MemberService memberService;

	@Mock
	private FileStorage fileStorage;

	@InjectMocks
	private PostService postService;

	@Test
	@DisplayName("사진 업로드 URL은 posts/ 아래 확장자가 붙은 키로 발급한다")
	void createImageUploadUrls_issuesKeys() {
		// given
		given(fileStorage.createUploadUrl(anyString(), eq("image/png"), eq(1024L)))
			.willReturn(new PresignedUpload("https://s3.test/upload", Instant.now()));

		// when
		List<PostImageUploadUrlResponse> responses = postService.createImageUploadUrls(
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
		List<ImageFile> files = Collections.nCopies(PostService.MAX_IMAGE_COUNT + 1, new ImageFile("image/jpeg", 1L));

		// when & then
		assertThatThrownBy(() -> postService.createImageUploadUrls(new PostImageUploadUrlRequest(files)))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.POST_IMAGE_LIMIT_EXCEEDED);
	}

	@Test
	@DisplayName("10MB를 넘거나 이미지가 아닌 파일은 업로드 URL을 발급하지 않는다")
	void createImageUploadUrls_invalidFile_throwsException() {
		assertThatThrownBy(() -> postService.createImageUploadUrls(new PostImageUploadUrlRequest(
			List.of(new ImageFile("image/jpeg", PostService.MAX_IMAGE_SIZE + 1)))))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.INVALID_POST_IMAGE);

		assertThatThrownBy(() -> postService.createImageUploadUrls(new PostImageUploadUrlRequest(
			List.of(new ImageFile("application/pdf", 1024L)))))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.INVALID_POST_IMAGE);
	}

	@Test
	@DisplayName("게시물을 저장하면 사진이 요청 순서대로 붙는다")
	void createPost_savesImagesInOrder() {
		// given
		given(memberService.getActiveMember(WRITER_ID)).willReturn(member(WRITER_ID));
		given(fileStorage.findSize(anyString())).willReturn(Optional.of(1024L));
		given(postRepository.save(any(Post.class))).willAnswer(invocation -> withId(invocation.getArgument(0)));

		// when
		postService.createPost(WRITER_ID, request(List.of("posts/a.jpg", "posts/b.jpg")));

		// then
		then(postRepository).should().save(argThat(post ->
			post.getImages().stream().map(PostImage::getImageKey).toList().equals(List.of("posts/a.jpg", "posts/b.jpg"))
				&& post.getImages().get(1).getSortOrder() == 1));
	}

	@Test
	@DisplayName("게시물 사진이 20장을 넘으면 저장하지 않는다")
	void createPost_overImageLimit_throwsException() {
		// given
		given(memberService.getActiveMember(WRITER_ID)).willReturn(member(WRITER_ID));
		List<String> imageKeys = IntStream.rangeClosed(0, PostService.MAX_IMAGE_COUNT)
			.mapToObj(i -> "posts/" + i + ".jpg")
			.toList();

		// when & then
		assertThatThrownBy(() -> postService.createPost(WRITER_ID, request(imageKeys)))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.POST_IMAGE_LIMIT_EXCEEDED);
		then(postRepository).should(never()).save(any());
	}

	@Test
	@DisplayName("업로드되지 않았거나 10MB를 넘거나 게시물 경로가 아니거나 중복된 사진 키는 거부한다")
	void createPost_invalidImageKey_throwsException() {
		// given
		given(memberService.getActiveMember(WRITER_ID)).willReturn(member(WRITER_ID));
		given(fileStorage.findSize("posts/missing.jpg")).willReturn(Optional.empty());
		given(fileStorage.findSize("posts/large.jpg")).willReturn(Optional.of(PostService.MAX_IMAGE_SIZE + 1));

		// when & then
		List<List<String>> invalidKeys = List.of(
			List.of("posts/missing.jpg"),
			List.of("posts/large.jpg"),
			List.of("reviews/a.jpg"),
			List.of("posts/a.jpg", "posts/a.jpg"));
		for (List<String> imageKeys : invalidKeys) {
			assertThatThrownBy(() -> postService.createPost(WRITER_ID, request(imageKeys)))
				.extracting("errorCode")
				.isEqualTo(ErrorCode.INVALID_POST_IMAGE);
		}
		then(postRepository).should(never()).save(any());
	}

	@Test
	@DisplayName("게시물을 수정하면 사진 목록을 요청 목록으로 바꾼다")
	void updatePost_replacesImages() {
		// given
		Post post = post(WRITER_ID, List.of("posts/a.jpg", "posts/b.jpg"));
		given(postRepository.findByIdAndDeletedAtIsNull(POST_ID)).willReturn(Optional.of(post));
		given(fileStorage.findSize(anyString())).willReturn(Optional.of(1024L));

		// when
		postService.updatePost(WRITER_ID, POST_ID, request(List.of("posts/c.jpg", "posts/a.jpg")));

		// then
		assertThat(post.getTitle()).isEqualTo("제목");
		assertThat(post.getImages()).extracting(PostImage::getImageKey, PostImage::getSortOrder)
			.containsExactly(tuple("posts/c.jpg", 0), tuple("posts/a.jpg", 1));
	}

	@Test
	@DisplayName("작성자가 아니면 게시물을 수정·삭제할 수 없다")
	void updateOrDelete_notWriter_forbidden() {
		// given
		Post post = post(WRITER_ID, List.of());
		given(postRepository.findByIdAndDeletedAtIsNull(POST_ID)).willReturn(Optional.of(post));

		// when & then
		assertThatThrownBy(() -> postService.updatePost(OTHER_MEMBER_ID, POST_ID, request(List.of())))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.FORBIDDEN);
		assertThatThrownBy(() -> postService.deletePost(OTHER_MEMBER_ID, POST_ID))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.FORBIDDEN);
		assertThat(post.isDeleted()).isFalse();
	}

	@Test
	@DisplayName("작성자가 삭제하면 soft delete 된다")
	void deletePost_byWriter_softDeletes() {
		// given
		Post post = post(WRITER_ID, List.of());
		given(postRepository.findByIdAndDeletedAtIsNull(POST_ID)).willReturn(Optional.of(post));

		// when
		postService.deletePost(WRITER_ID, POST_ID);

		// then
		assertThat(post.isDeleted()).isTrue();
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

	private static PostSaveRequest request(List<String> imageKeys) {
		return new PostSaveRequest(PostCategory.NEWS, "제목", "본문", imageKeys);
	}

	private static Member member(Long id) {
		Member member = Member.builder().provider(Provider.KAKAO).providerId("p" + id).name("몽키").build();
		ReflectionTestUtils.setField(member, "id", id);
		return member;
	}

	private static Post post(Long writerId, List<String> imageKeys) {
		Post post = Post.builder()
			.member(member(writerId))
			.category(PostCategory.GENERAL)
			.title("원래 제목")
			.content("원래 본문")
			.build();
		imageKeys.forEach(post::addImage);
		return withId(post);
	}

	private static Post withId(Post post) {
		ReflectionTestUtils.setField(post, "id", POST_ID);
		return post;
	}
}
