package com.mongkids.domain.post.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

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
import com.mongkids.domain.post.entity.LabCategory;
import com.mongkids.domain.post.entity.LabPost;
import com.mongkids.domain.post.entity.LabPostImage;
import com.mongkids.domain.post.repository.LabPostRepository;
import com.mongkids.global.exception.ErrorCode;
import com.mongkids.global.storage.FileStorage;

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
			+ "<figure class=\"post-photos\"><img data-image-key=\"posts/a.jpg\"><img data-image-key=\"posts/b.jpg\"></figure>"));

		// then
		then(labPostRepository).should().save(argThat(labPost ->
			labPost.getCategory() == LabCategory.PARENT_QA
				&& !labPost.getContent().contains("onclick")
				&& labPost.getImages().stream().map(LabPostImage::getImageKey).toList()
				.equals(List.of("posts/a.jpg", "posts/b.jpg"))));
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
