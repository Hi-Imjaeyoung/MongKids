package com.mongkids.domain.post.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.entity.MemberRole;
import com.mongkids.domain.member.entity.Provider;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.domain.post.dto.PostCommentResponse;
import com.mongkids.domain.post.dto.PostCommentSaveRequest;
import com.mongkids.domain.post.entity.Post;
import com.mongkids.domain.post.entity.PostCategory;
import com.mongkids.domain.post.entity.PostComment;
import com.mongkids.domain.post.repository.PostCommentRepository;
import com.mongkids.domain.post.repository.PostRepository;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class PostCommentServiceTest {

	private static final Long AUTHOR_ID = 1L;
	private static final Long OTHER_MEMBER_ID = 2L;
	private static final Long ADMIN_ID = 3L;
	private static final Long POST_ID = 10L;
	private static final Long COMMENT_ID = 100L;

	@Mock
	private PostCommentRepository commentRepository;

	@Mock
	private PostRepository postRepository;

	@Mock
	private MemberService memberService;

	@InjectMocks
	private PostCommentService commentService;

	@Test
	@DisplayName("직원이 아닌 회원도 댓글을 쓸 수 있고, 쓰면 게시물 댓글 수가 늘어난다")
	void createComment_byMember_increasesCount() {
		// given
		given(memberService.getActiveMember(AUTHOR_ID)).willReturn(member(AUTHOR_ID, MemberRole.MEMBER));
		given(postRepository.findByIdAndDeletedAtIsNull(POST_ID)).willReturn(Optional.of(post()));
		given(commentRepository.save(any(PostComment.class))).willAnswer(invocation -> {
			PostComment comment = invocation.getArgument(0);
			ReflectionTestUtils.setField(comment, "id", COMMENT_ID);
			return comment;
		});

		// when
		PostCommentResponse response = commentService.createComment(AUTHOR_ID, POST_ID,
			new PostCommentSaveRequest("재밌어 보여요"));

		// then
		assertThat(response.id()).isEqualTo(COMMENT_ID);
		assertThat(response.content()).isEqualTo("재밌어 보여요");
		assertThat(response.authorId()).isEqualTo(AUTHOR_ID);
		then(postRepository).should().increaseCommentCount(POST_ID);
	}

	@Test
	@DisplayName("없거나 삭제된 게시물에는 댓글을 쓸 수 없다")
	void createComment_postNotFound_throwsException() {
		// given
		given(memberService.getActiveMember(AUTHOR_ID)).willReturn(member(AUTHOR_ID, MemberRole.MEMBER));
		given(postRepository.findByIdAndDeletedAtIsNull(POST_ID)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> commentService.createComment(AUTHOR_ID, POST_ID, new PostCommentSaveRequest("댓글")))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.POST_NOT_FOUND);
		then(commentRepository).should(never()).save(any());
		then(postRepository).should(never()).increaseCommentCount(any());
	}

	@Test
	@DisplayName("목록은 오래된 순으로 조회하고 크기는 최대 100으로 맞춘다")
	void getComments_oldestFirst() {
		// given
		given(postRepository.findByIdAndDeletedAtIsNull(POST_ID)).willReturn(Optional.of(post()));
		given(commentRepository.findByPostIdAndDeletedAtIsNull(eq(POST_ID), any(Pageable.class)))
			.willReturn(Page.empty());

		// when
		commentService.getComments(POST_ID, PageRequest.of(1, 500));

		// then
		then(commentRepository).should().findByPostIdAndDeletedAtIsNull(eq(POST_ID), argThat(pageable ->
			pageable.getPageNumber() == 1
				&& pageable.getPageSize() == PostService.MAX_PAGE_SIZE
				&& pageable.getSort().equals(PostCommentService.OLDEST)));
	}

	@Test
	@DisplayName("작성자는 자기 댓글을 지울 수 있고, 지우면 게시물 댓글 수가 줄어든다")
	void deleteComment_byAuthor_decreasesCount() {
		// given
		PostComment comment = comment(AUTHOR_ID);
		given(memberService.getActiveMember(AUTHOR_ID)).willReturn(member(AUTHOR_ID, MemberRole.MEMBER));
		given(postRepository.findByIdAndDeletedAtIsNull(POST_ID)).willReturn(Optional.of(post()));
		given(commentRepository.findByIdAndPostIdAndDeletedAtIsNull(COMMENT_ID, POST_ID))
			.willReturn(Optional.of(comment));

		// when
		commentService.deleteComment(AUTHOR_ID, POST_ID, COMMENT_ID);

		// then
		assertThat(comment.isDeleted()).isTrue();
		then(postRepository).should().decreaseCommentCount(POST_ID);
	}

	@Test
	@DisplayName("관리자는 다른 회원의 댓글도 지울 수 있다")
	void deleteComment_byAdmin() {
		// given
		PostComment comment = comment(AUTHOR_ID);
		given(memberService.getActiveMember(ADMIN_ID)).willReturn(member(ADMIN_ID, MemberRole.ADMIN));
		given(postRepository.findByIdAndDeletedAtIsNull(POST_ID)).willReturn(Optional.of(post()));
		given(commentRepository.findByIdAndPostIdAndDeletedAtIsNull(COMMENT_ID, POST_ID))
			.willReturn(Optional.of(comment));

		// when
		commentService.deleteComment(ADMIN_ID, POST_ID, COMMENT_ID);

		// then
		assertThat(comment.isDeleted()).isTrue();
	}

	@Test
	@DisplayName("다른 회원의 댓글은 직원이어도 지울 수 없다")
	void deleteComment_byOther_forbidden() {
		// given
		PostComment comment = comment(AUTHOR_ID);
		given(memberService.getActiveMember(OTHER_MEMBER_ID)).willReturn(member(OTHER_MEMBER_ID, MemberRole.STAFF));
		given(postRepository.findByIdAndDeletedAtIsNull(POST_ID)).willReturn(Optional.of(post()));
		given(commentRepository.findByIdAndPostIdAndDeletedAtIsNull(COMMENT_ID, POST_ID))
			.willReturn(Optional.of(comment));

		// when & then
		assertThatThrownBy(() -> commentService.deleteComment(OTHER_MEMBER_ID, POST_ID, COMMENT_ID))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.FORBIDDEN);
		assertThat(comment.isDeleted()).isFalse();
		then(postRepository).should(never()).decreaseCommentCount(any());
	}

	@Test
	@DisplayName("없거나 이미 지운 댓글은 P006으로 응답한다")
	void deleteComment_notFound_throwsException() {
		// given
		given(memberService.getActiveMember(AUTHOR_ID)).willReturn(member(AUTHOR_ID, MemberRole.MEMBER));
		given(postRepository.findByIdAndDeletedAtIsNull(POST_ID)).willReturn(Optional.of(post()));
		given(commentRepository.findByIdAndPostIdAndDeletedAtIsNull(COMMENT_ID, POST_ID)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> commentService.deleteComment(AUTHOR_ID, POST_ID, COMMENT_ID))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
	}

	private static Member member(Long id, MemberRole role) {
		Member member = Member.builder().provider(Provider.KAKAO).providerId("p" + id).name("몽키").build();
		ReflectionTestUtils.setField(member, "id", id);
		ReflectionTestUtils.setField(member, "role", role);
		return member;
	}

	private static Post post() {
		Post post = Post.builder()
			.member(member(ADMIN_ID, MemberRole.ADMIN))
			.category(PostCategory.GENERAL)
			.title("제목")
			.content("<p>본문</p>")
			.build();
		ReflectionTestUtils.setField(post, "id", POST_ID);
		return post;
	}

	private static PostComment comment(Long authorId) {
		PostComment comment = PostComment.builder()
			.post(post())
			.member(member(authorId, MemberRole.MEMBER))
			.content("댓글")
			.build();
		ReflectionTestUtils.setField(comment, "id", COMMENT_ID);
		return comment;
	}
}
