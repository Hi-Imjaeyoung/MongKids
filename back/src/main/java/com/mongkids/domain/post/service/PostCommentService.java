package com.mongkids.domain.post.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.domain.post.dto.PostCommentResponse;
import com.mongkids.domain.post.dto.PostCommentSaveRequest;
import com.mongkids.domain.post.entity.Post;
import com.mongkids.domain.post.entity.PostComment;
import com.mongkids.domain.post.repository.PostCommentRepository;
import com.mongkids.domain.post.repository.PostRepository;
import com.mongkids.global.common.PageResponse;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 수업 현장 게시물 댓글 (FR-010). 로그인한 회원이면 누구나 쓸 수 있다. 연구소 글에는 댓글이 없다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostCommentService {

	static final Sort OLDEST = Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id"));

	private final PostCommentRepository commentRepository;
	private final PostRepository postRepository;
	private final MemberService memberService;

	@Transactional
	public PostCommentResponse createComment(Long memberId, Long postId, PostCommentSaveRequest request) {
		Member member = memberService.getActiveMember(memberId);
		Post post = getActivePost(postId);

		PostComment comment = commentRepository.save(PostComment.builder()
			.post(post)
			.member(member)
			.content(request.content())
			.build());
		postRepository.increaseCommentCount(postId);
		return PostCommentResponse.from(comment);
	}

	/**
	 * 오래된 순 목록. 대화 흐름대로 읽히도록 먼저 쓴 댓글이 위에 온다.
	 */
	public PageResponse<PostCommentResponse> getComments(Long postId, Pageable pageable) {
		getActivePost(postId);
		Pageable oldest = PageRequest.of(pageable.getPageNumber(),
			Math.min(pageable.getPageSize(), PostService.MAX_PAGE_SIZE), OLDEST);
		return PageResponse.from(commentRepository.findByPostIdAndDeletedAtIsNull(postId, oldest),
			PostCommentResponse::from);
	}

	/**
	 * 작성자 본인 또는 관리자만 지울 수 있다.
	 */
	@Transactional
	public void deleteComment(Long memberId, Long postId, Long commentId) {
		Member member = memberService.getActiveMember(memberId);
		getActivePost(postId);
		PostComment comment = commentRepository.findByIdAndPostIdAndDeletedAtIsNull(commentId, postId)
			.orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
		if (!comment.getMember().getId().equals(member.getId()) && !member.getRole().isAdmin()) {
			throw new BusinessException(ErrorCode.FORBIDDEN);
		}

		comment.delete();
		postRepository.decreaseCommentCount(postId);
	}

	private Post getActivePost(Long postId) {
		return postRepository.findByIdAndDeletedAtIsNull(postId)
			.orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
	}
}
