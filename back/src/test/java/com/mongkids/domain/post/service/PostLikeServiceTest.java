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
import org.springframework.test.util.ReflectionTestUtils;

import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.entity.MemberRole;
import com.mongkids.domain.member.entity.Provider;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.domain.post.dto.LikeResponse;
import com.mongkids.domain.post.entity.LabCategory;
import com.mongkids.domain.post.entity.LabPost;
import com.mongkids.domain.post.entity.LabPostLike;
import com.mongkids.domain.post.entity.Post;
import com.mongkids.domain.post.entity.PostCategory;
import com.mongkids.domain.post.entity.PostLike;
import com.mongkids.domain.post.repository.LabPostLikeRepository;
import com.mongkids.domain.post.repository.LabPostRepository;
import com.mongkids.domain.post.repository.PostLikeRepository;
import com.mongkids.domain.post.repository.PostRepository;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class PostLikeServiceTest {

	private static final Long MEMBER_ID = 1L;
	private static final Long WRITER_ID = 2L;
	private static final Long POST_ID = 10L;

	@Mock
	private PostRepository postRepository;

	@Mock
	private LabPostRepository labPostRepository;

	@Mock
	private PostLikeRepository postLikeRepository;

	@Mock
	private LabPostLikeRepository labPostLikeRepository;

	@Mock
	private MemberService memberService;

	@InjectMocks
	private PostLikeService likeService;

	@Test
	@DisplayName("직원이 아닌 회원도 공감할 수 있고, 공감하면 공감 수가 1 늘어난다")
	void likePost_byMember_increasesCount() {
		// given
		Post post = post();
		given(memberService.getActiveMember(MEMBER_ID)).willReturn(member(MEMBER_ID, MemberRole.MEMBER));
		given(postRepository.findForLikeById(POST_ID)).willReturn(Optional.of(post));
		given(postLikeRepository.existsByPostIdAndMemberId(POST_ID, MEMBER_ID)).willReturn(false);

		// when
		LikeResponse response = likeService.likePost(MEMBER_ID, POST_ID);

		// then
		assertThat(response).isEqualTo(new LikeResponse(true, 1));
		then(postLikeRepository).should().save(any(PostLike.class));
	}

	@Test
	@DisplayName("이미 공감한 게시물에 다시 공감하면 아무것도 바꾸지 않는다")
	void likePost_alreadyLiked_noChange() {
		// given
		Post post = post();
		post.increaseLikeCount();
		given(memberService.getActiveMember(MEMBER_ID)).willReturn(member(MEMBER_ID, MemberRole.MEMBER));
		given(postRepository.findForLikeById(POST_ID)).willReturn(Optional.of(post));
		given(postLikeRepository.existsByPostIdAndMemberId(POST_ID, MEMBER_ID)).willReturn(true);

		// when
		LikeResponse response = likeService.likePost(MEMBER_ID, POST_ID);

		// then
		assertThat(response).isEqualTo(new LikeResponse(true, 1));
		then(postLikeRepository).should(never()).save(any());
	}

	@Test
	@DisplayName("공감을 취소하면 공감 수가 1 줄고, 공감하지 않은 게시물을 취소하면 그대로다")
	void unlikePost() {
		// given
		Post post = post();
		post.increaseLikeCount();
		given(memberService.getActiveMember(MEMBER_ID)).willReturn(member(MEMBER_ID, MemberRole.MEMBER));
		given(postRepository.findForLikeById(POST_ID)).willReturn(Optional.of(post));
		given(postLikeRepository.deleteByPostIdAndMemberId(POST_ID, MEMBER_ID)).willReturn(1L, 0L);

		// when & then
		assertThat(likeService.unlikePost(MEMBER_ID, POST_ID)).isEqualTo(new LikeResponse(false, 0));
		assertThat(likeService.unlikePost(MEMBER_ID, POST_ID)).isEqualTo(new LikeResponse(false, 0));
	}

	@Test
	@DisplayName("비로그인 조회는 공감 여부를 확인하지 않고 liked=false로 응답한다")
	void getPostLike_anonymous() {
		// given
		given(postRepository.findByIdAndDeletedAtIsNull(POST_ID)).willReturn(Optional.of(post()));

		// when
		LikeResponse response = likeService.getPostLike(null, POST_ID);

		// then
		assertThat(response).isEqualTo(new LikeResponse(false, 0));
		then(postLikeRepository).should(never()).existsByPostIdAndMemberId(any(), any());
	}

	@Test
	@DisplayName("없거나 삭제된 게시물에는 공감할 수 없다")
	void likePost_postNotFound_throwsException() {
		// given
		given(memberService.getActiveMember(MEMBER_ID)).willReturn(member(MEMBER_ID, MemberRole.MEMBER));
		given(postRepository.findForLikeById(POST_ID)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> likeService.likePost(MEMBER_ID, POST_ID))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.POST_NOT_FOUND);
		then(postLikeRepository).should(never()).save(any());
	}

	@Test
	@DisplayName("연구소 글도 공감할 수 있고, 없는 연구소 글은 P005로 응답한다")
	void likeLabPost() {
		// given
		LabPost labPost = labPost();
		given(memberService.getActiveMember(MEMBER_ID)).willReturn(member(MEMBER_ID, MemberRole.MEMBER));
		given(labPostRepository.findForLikeById(POST_ID)).willReturn(Optional.of(labPost), Optional.empty());
		given(labPostLikeRepository.existsByLabPostIdAndMemberId(POST_ID, MEMBER_ID)).willReturn(false);

		// when & then
		assertThat(likeService.likeLabPost(MEMBER_ID, POST_ID)).isEqualTo(new LikeResponse(true, 1));
		then(labPostLikeRepository).should().save(any(LabPostLike.class));
		assertThatThrownBy(() -> likeService.likeLabPost(MEMBER_ID, POST_ID))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.LAB_POST_NOT_FOUND);
	}

	private static Member member(Long id, MemberRole role) {
		Member member = Member.builder().provider(Provider.KAKAO).providerId("p" + id).name("몽키").build();
		ReflectionTestUtils.setField(member, "id", id);
		ReflectionTestUtils.setField(member, "role", role);
		return member;
	}

	private static Post post() {
		Post post = Post.builder()
			.member(member(WRITER_ID, MemberRole.STAFF))
			.category(PostCategory.GENERAL)
			.title("제목")
			.content("<p>본문</p>")
			.build();
		ReflectionTestUtils.setField(post, "id", POST_ID);
		return post;
	}

	private static LabPost labPost() {
		LabPost labPost = LabPost.builder()
			.member(member(WRITER_ID, MemberRole.STAFF))
			.category(LabCategory.GROWTH)
			.title("제목")
			.content("<p>본문</p>")
			.build();
		ReflectionTestUtils.setField(labPost, "id", POST_ID);
		return labPost;
	}
}
