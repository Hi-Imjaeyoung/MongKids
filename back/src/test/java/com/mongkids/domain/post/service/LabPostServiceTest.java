package com.mongkids.domain.post.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
import com.mongkids.domain.post.dto.LabPostSaveRequest;
import com.mongkids.domain.post.dto.PostImageUploadUrlRequest;
import com.mongkids.domain.post.dto.PostImageUploadUrlRequest.ImageFile;
import com.mongkids.domain.post.dto.PostImageUploadUrlResponse;
import com.mongkids.domain.post.entity.LabCategory;
import com.mongkids.domain.post.entity.LabPost;
import com.mongkids.domain.post.entity.LabPostImage;
import com.mongkids.domain.post.repository.LabPostRepository;
import com.mongkids.global.exception.ErrorCode;
import com.mongkids.global.storage.FileStorage;
import com.mongkids.global.storage.PresignedUpload;

@ExtendWith(MockitoExtension.class)
class LabPostServiceTest {

	private static final Long WRITER_ID = 1L;
	private static final Long OTHER_MEMBER_ID = 2L;
	private static final Long ADMIN_ID = 3L;
	private static final Long LAB_POST_ID = 10L;

	@Mock
	private LabPostRepository labPostRepository;

	@Mock
	private MemberService memberService;

	@Mock
	private FileStorage fileStorage;

	@Spy
	private PostContentSanitizer contentSanitizer = new PostContentSanitizer();

	private LabPostService labPostService;

	@BeforeEach
	void setUp() {
		ArticleWriteSupport writeSupport = new ArticleWriteSupport(memberService, fileStorage, contentSanitizer);
		labPostService = new LabPostService(labPostRepository, writeSupport, fileStorage, contentSanitizer);
	}

	@Test
	@DisplayName("연구소 사진 업로드 URL은 lab/ 아래 확장자가 붙은 키로 발급한다")
	void createImageUploadUrls_issuesLabKeys() {
		// given
		given(memberService.getActiveMember(WRITER_ID)).willReturn(member(WRITER_ID, MemberRole.STAFF));
		given(fileStorage.createUploadUrl(anyString(), eq("image/webp"), eq(1024L)))
			.willReturn(new PresignedUpload("https://s3.test/upload", Instant.now()));

		// when
		List<PostImageUploadUrlResponse> responses = labPostService.createImageUploadUrls(WRITER_ID,
			new PostImageUploadUrlRequest(List.of(new ImageFile("image/webp", 1024L))));

		// then
		assertThat(responses).singleElement()
			.satisfies(response -> assertThat(response.imageKey()).startsWith("lab/").endsWith(".webp"));
	}

	@Test
	@DisplayName("직원 권한이 없는 회원은 연구소 글을 작성할 수 없다")
	void createLabPost_notStaff_forbidden() {
		// given
		given(memberService.getActiveMember(WRITER_ID)).willReturn(member(WRITER_ID, MemberRole.MEMBER));

		// when & then
		assertThatThrownBy(() -> labPostService.createLabPost(WRITER_ID, request("<p>글</p>")))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.FORBIDDEN);
		then(labPostRepository).should(never()).save(any());
	}

	@Test
	@DisplayName("연구소 글을 저장하면 본문을 정화하고 본문 속 사진이 순서대로 붙는다")
	void createLabPost_savesImagesInOrder() {
		// given
		given(memberService.getActiveMember(WRITER_ID)).willReturn(member(WRITER_ID, MemberRole.STAFF));
		given(fileStorage.findSize(anyString())).willReturn(Optional.of(1024L));
		given(labPostRepository.save(any(LabPost.class))).willAnswer(invocation -> withId(invocation.getArgument(0)));

		// when
		labPostService.createLabPost(WRITER_ID, request("<p onclick=\"x()\">글</p>"
			+ "<figure class=\"post-photos\"><img data-image-key=\"lab/a.jpg\"><img data-image-key=\"lab/b.jpg\"></figure>"));

		// then
		then(labPostRepository).should().save(argThat(labPost ->
			labPost.getCategory() == LabCategory.PARENT_QA
				&& !labPost.getContent().contains("onclick")
				&& labPost.getImages().stream().map(LabPostImage::getImageKey).toList()
				.equals(List.of("lab/a.jpg", "lab/b.jpg"))));
	}

	@Test
	@DisplayName("수업 현장 경로(posts/)의 사진으로는 연구소 글을 작성할 수 없다")
	void createLabPost_postImageKey_throwsException() {
		// given
		given(memberService.getActiveMember(WRITER_ID)).willReturn(member(WRITER_ID, MemberRole.STAFF));

		// when & then
		assertThatThrownBy(() -> labPostService.createLabPost(WRITER_ID, request(photo("posts/a.jpg"))))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.INVALID_POST_IMAGE);
		then(labPostRepository).should(never()).save(any());
	}

	@Test
	@DisplayName("경로를 나누기 전 posts/에 올린 사진은 그 글을 수정할 때 유지할 수 있지만, 새로 넣을 수는 없다")
	void updateLabPost_keepsLegacyImageOnly() {
		// given
		LabPost labPost = labPost(WRITER_ID);
		labPost.addImage("posts/old.jpg");
		given(memberService.getActiveMember(WRITER_ID)).willReturn(member(WRITER_ID, MemberRole.STAFF));
		given(labPostRepository.findByIdAndDeletedAtIsNull(LAB_POST_ID)).willReturn(Optional.of(labPost));
		given(fileStorage.findSize(anyString())).willReturn(Optional.of(1024L));

		// when
		labPostService.updateLabPost(WRITER_ID, LAB_POST_ID, request(photo("posts/old.jpg") + photo("lab/new.jpg")));

		// then
		assertThat(labPost.getImages().stream().map(LabPostImage::getImageKey).toList())
			.containsExactly("posts/old.jpg", "lab/new.jpg");
		assertThatThrownBy(() -> labPostService.updateLabPost(WRITER_ID, LAB_POST_ID, request(photo("posts/other.jpg"))))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.INVALID_POST_IMAGE);
	}

	@Test
	@DisplayName("다른 직원의 연구소 글은 수정할 수 없다")
	void updateLabPost_otherStaff_forbidden() {
		// given
		LabPost labPost = labPost(WRITER_ID);
		given(memberService.getActiveMember(OTHER_MEMBER_ID)).willReturn(member(OTHER_MEMBER_ID, MemberRole.STAFF));
		given(labPostRepository.findByIdAndDeletedAtIsNull(LAB_POST_ID)).willReturn(Optional.of(labPost));

		// when & then
		assertThatThrownBy(() -> labPostService.updateLabPost(OTHER_MEMBER_ID, LAB_POST_ID, request("<p>수정</p>")))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.FORBIDDEN);
		assertThat(labPost.getTitle()).isEqualTo("원래 제목");
	}

	@Test
	@DisplayName("관리자는 다른 직원의 연구소 글을 삭제할 수 있다")
	void deleteLabPost_admin_deletes() {
		// given
		LabPost labPost = labPost(WRITER_ID);
		given(memberService.getActiveMember(ADMIN_ID)).willReturn(member(ADMIN_ID, MemberRole.ADMIN));
		given(labPostRepository.findByIdAndDeletedAtIsNull(LAB_POST_ID)).willReturn(Optional.of(labPost));

		// when
		labPostService.deleteLabPost(ADMIN_ID, LAB_POST_ID);

		// then
		assertThat(labPost.isDeleted()).isTrue();
	}

	@Test
	@DisplayName("없거나 삭제된 연구소 글을 조회하면 LAB_POST_NOT_FOUND 예외가 발생한다")
	void getLabPost_notFound_throwsException() {
		// given
		given(labPostRepository.findByIdAndDeletedAtIsNull(LAB_POST_ID)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> labPostService.getLabPost(LAB_POST_ID))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.LAB_POST_NOT_FOUND);
	}

	private static String photo(String imageKey) {
		return "<figure class=\"post-photos\"><img data-image-key=\"" + imageKey + "\"></figure>";
	}

	private static LabPostSaveRequest request(String content) {
		return new LabPostSaveRequest(LabCategory.PARENT_QA, "제목", content);
	}

	private static Member member(Long id, MemberRole role) {
		Member member = Member.builder().provider(Provider.KAKAO).providerId("p" + id).name("몽키").build();
		ReflectionTestUtils.setField(member, "id", id);
		ReflectionTestUtils.setField(member, "role", role);
		return member;
	}

	private static LabPost labPost(Long writerId) {
		return withId(LabPost.builder()
			.member(member(writerId, MemberRole.STAFF))
			.category(LabCategory.GROWTH)
			.title("원래 제목")
			.content("<p>원래 본문</p>")
			.build());
	}

	private static LabPost withId(LabPost labPost) {
		ReflectionTestUtils.setField(labPost, "id", LAB_POST_ID);
		return labPost;
	}
}
