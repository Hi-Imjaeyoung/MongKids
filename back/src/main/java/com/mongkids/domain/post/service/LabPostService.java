package com.mongkids.domain.post.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.post.dto.LabPostDetailResponse;
import com.mongkids.domain.post.dto.LabPostSaveRequest;
import com.mongkids.domain.post.dto.LabPostSummaryResponse;
import com.mongkids.domain.post.dto.PostCreateResponse;
import com.mongkids.domain.post.entity.LabCategory;
import com.mongkids.domain.post.entity.LabPost;
import com.mongkids.domain.post.repository.LabPostRepository;
import com.mongkids.domain.post.service.PostContentSanitizer.SanitizedContent;
import com.mongkids.global.common.PageResponse;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;
import com.mongkids.global.storage.FileStorage;

import lombok.RequiredArgsConstructor;

/**
 * 몽키즈 연구소 글 (FR-008, SCR-024). 권한·본문·사진 규칙은 수업 현장 게시물과 같다 (FR-009).
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LabPostService {

	private final LabPostRepository labPostRepository;
	private final ArticleWriteSupport writeSupport;
	private final FileStorage fileStorage;
	private final PostContentSanitizer contentSanitizer;

	@Transactional
	public PostCreateResponse createLabPost(Long memberId, LabPostSaveRequest request) {
		Member member = writeSupport.getWriter(memberId);
		SanitizedContent content = writeSupport.sanitizeContent(request.content());

		LabPost labPost = LabPost.builder()
			.member(member)
			.category(request.category())
			.title(request.title())
			.content(content.html())
			.build();
		content.imageKeys().forEach(labPost::addImage);
		return new PostCreateResponse(labPostRepository.save(labPost).getId());
	}

	/**
	 * 최신순 목록. 분류가 없으면 전체 탭이다.
	 */
	public PageResponse<LabPostSummaryResponse> getLabPosts(LabCategory category, Pageable pageable) {
		Pageable latest = PostService.latest(pageable);
		Page<LabPost> labPosts = category == null
			? labPostRepository.findByDeletedAtIsNull(latest)
			: labPostRepository.findByCategoryAndDeletedAtIsNull(category, latest);
		return PageResponse.from(labPosts, labPost -> LabPostSummaryResponse.of(labPost, fileStorage::getUrl));
	}

	public LabPostDetailResponse getLabPost(Long labPostId) {
		LabPost labPost = getActiveLabPost(labPostId);
		return LabPostDetailResponse.of(labPost, contentSanitizer.render(labPost.getContent(), fileStorage::getUrl));
	}

	/**
	 * 연구소 글을 수정한다. 사진 목록은 새 본문에 나온 사진으로 통째로 바꾼다.
	 */
	@Transactional
	public void updateLabPost(Long memberId, Long labPostId, LabPostSaveRequest request) {
		LabPost labPost = getWritableLabPost(memberId, labPostId);
		SanitizedContent content = writeSupport.sanitizeContent(request.content());

		labPost.update(request.category(), request.title(), content.html());
		labPost.clearImages();
		content.imageKeys().forEach(labPost::addImage);
	}

	@Transactional
	public void deleteLabPost(Long memberId, Long labPostId) {
		getWritableLabPost(memberId, labPostId).delete();
	}

	private LabPost getActiveLabPost(Long labPostId) {
		return labPostRepository.findByIdAndDeletedAtIsNull(labPostId)
			.orElseThrow(() -> new BusinessException(ErrorCode.LAB_POST_NOT_FOUND));
	}

	/**
	 * 수정·삭제할 수 있는 연구소 글: 직원 권한이 있는 작성자 본인이거나 관리자.
	 * 권한을 먼저 확인해 직원이 아니면 글을 조회하지 않는다.
	 */
	private LabPost getWritableLabPost(Long memberId, Long labPostId) {
		Member member = writeSupport.getWriter(memberId);
		LabPost labPost = getActiveLabPost(labPostId);
		writeSupport.checkWritable(member, labPost);
		return labPost;
	}
}
