package com.mongkids.domain.post.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.domain.post.dto.LikeResponse;
import com.mongkids.domain.post.entity.LabPost;
import com.mongkids.domain.post.entity.LabPostLike;
import com.mongkids.domain.post.entity.Post;
import com.mongkids.domain.post.entity.PostLike;
import com.mongkids.domain.post.repository.LabPostLikeRepository;
import com.mongkids.domain.post.repository.LabPostRepository;
import com.mongkids.domain.post.repository.PostLikeRepository;
import com.mongkids.domain.post.repository.PostRepository;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 수업 현장 게시물과 몽키즈 연구소 글의 공감 (FR-011). 로그인한 회원이면 누구나, 글마다 한 번씩 공감·취소할 수 있다.
 * 공감·취소는 글 행을 잠근 뒤 처리해 같은 회원이 동시에 눌러도 한 번만 반영된다.
 * 이미 공감한 글에 공감하거나 공감하지 않은 글을 취소하면 아무것도 바꾸지 않고 현재 상태를 돌려준다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostLikeService {

	private final PostRepository postRepository;
	private final LabPostRepository labPostRepository;
	private final PostLikeRepository postLikeRepository;
	private final LabPostLikeRepository labPostLikeRepository;
	private final MemberService memberService;

	/**
	 * @param memberId 비로그인이면 null
	 */
	public LikeResponse getPostLike(Long memberId, Long postId) {
		Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
			.orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
		boolean liked = memberId != null && postLikeRepository.existsByPostIdAndMemberId(postId, memberId);
		return new LikeResponse(liked, post.getLikeCount());
	}

	@Transactional
	public LikeResponse likePost(Long memberId, Long postId) {
		Member member = memberService.getActiveMember(memberId);
		Post post = getPostForLike(postId);
		if (!postLikeRepository.existsByPostIdAndMemberId(postId, memberId)) {
			postLikeRepository.save(PostLike.builder().post(post).member(member).build());
			post.increaseLikeCount();
		}
		return new LikeResponse(true, post.getLikeCount());
	}

	@Transactional
	public LikeResponse unlikePost(Long memberId, Long postId) {
		memberService.getActiveMember(memberId);
		Post post = getPostForLike(postId);
		if (postLikeRepository.deleteByPostIdAndMemberId(postId, memberId) > 0) {
			post.decreaseLikeCount();
		}
		return new LikeResponse(false, post.getLikeCount());
	}

	/**
	 * @param memberId 비로그인이면 null
	 */
	public LikeResponse getLabPostLike(Long memberId, Long labPostId) {
		LabPost labPost = labPostRepository.findByIdAndDeletedAtIsNull(labPostId)
			.orElseThrow(() -> new BusinessException(ErrorCode.LAB_POST_NOT_FOUND));
		boolean liked = memberId != null && labPostLikeRepository.existsByLabPostIdAndMemberId(labPostId, memberId);
		return new LikeResponse(liked, labPost.getLikeCount());
	}

	@Transactional
	public LikeResponse likeLabPost(Long memberId, Long labPostId) {
		Member member = memberService.getActiveMember(memberId);
		LabPost labPost = getLabPostForLike(labPostId);
		if (!labPostLikeRepository.existsByLabPostIdAndMemberId(labPostId, memberId)) {
			labPostLikeRepository.save(LabPostLike.builder().labPost(labPost).member(member).build());
			labPost.increaseLikeCount();
		}
		return new LikeResponse(true, labPost.getLikeCount());
	}

	@Transactional
	public LikeResponse unlikeLabPost(Long memberId, Long labPostId) {
		memberService.getActiveMember(memberId);
		LabPost labPost = getLabPostForLike(labPostId);
		if (labPostLikeRepository.deleteByLabPostIdAndMemberId(labPostId, memberId) > 0) {
			labPost.decreaseLikeCount();
		}
		return new LikeResponse(false, labPost.getLikeCount());
	}

	private Post getPostForLike(Long postId) {
		return postRepository.findForLikeById(postId)
			.orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
	}

	private LabPost getLabPostForLike(Long labPostId) {
		return labPostRepository.findForLikeById(labPostId)
			.orElseThrow(() -> new BusinessException(ErrorCode.LAB_POST_NOT_FOUND));
	}
}
