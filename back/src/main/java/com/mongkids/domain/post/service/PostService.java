package com.mongkids.domain.post.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.post.dto.PostCreateResponse;
import com.mongkids.domain.post.dto.PostDetailResponse;
import com.mongkids.domain.post.dto.PostImageUploadUrlRequest;
import com.mongkids.domain.post.dto.PostImageUploadUrlResponse;
import com.mongkids.domain.post.dto.PostSaveRequest;
import com.mongkids.domain.post.dto.PostSummaryResponse;
import com.mongkids.domain.post.entity.Post;
import com.mongkids.domain.post.entity.PostCategory;
import com.mongkids.domain.post.repository.PostRepository;
import com.mongkids.domain.post.service.PostContentSanitizer.SanitizedContent;
import com.mongkids.global.common.PageResponse;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;
import com.mongkids.global.storage.FileStorage;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

	static final int MAX_PAGE_SIZE = 100;
	static final Sort LATEST = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

	private final PostRepository postRepository;
	private final ArticleWriteSupport writeSupport;
	private final FileStorage fileStorage;
	private final PostContentSanitizer contentSanitizer;

	/**
	 * 사진을 S3 {@code posts/} 아래에 직접 올릴 URL을 발급한다 (FR-008). 연구소 글은 {@code lab/}로 따로 발급한다.
	 */
	public List<PostImageUploadUrlResponse> createImageUploadUrls(Long memberId, PostImageUploadUrlRequest request) {
		return writeSupport.createImageUploadUrls(memberId, request, ArticleWriteSupport.POST_IMAGE_KEY_PREFIX);
	}

	@Transactional
	public PostCreateResponse createPost(Long memberId, PostSaveRequest request) {
		Member member = writeSupport.getWriter(memberId);
		SanitizedContent content = writeSupport.sanitizeContent(request.content(), ArticleWriteSupport.POST_IMAGE_KEY_PREFIX);

		Post post = Post.builder()
			.member(member)
			.category(request.category())
			.title(request.title())
			.content(content.html())
			.build();
		content.imageKeys().forEach(post::addImage);
		return new PostCreateResponse(postRepository.save(post).getId());
	}

	/**
	 * 최신순 목록. 분류가 없으면 전체 탭이다.
	 */
	public PageResponse<PostSummaryResponse> getPosts(PostCategory category, Pageable pageable) {
		Pageable latest = latest(pageable);
		Page<Post> posts = category == null
			? postRepository.findByDeletedAtIsNull(latest)
			: postRepository.findByCategoryAndDeletedAtIsNull(category, latest);
		return PageResponse.from(posts, post -> PostSummaryResponse.of(post, fileStorage::getUrl));
	}

	public PostDetailResponse getPost(Long postId) {
		Post post = getActivePost(postId);
		return PostDetailResponse.of(post, contentSanitizer.render(post.getContent(), fileStorage::getUrl));
	}

	/**
	 * 게시물을 수정한다. 사진 목록은 새 본문에 나온 사진으로 통째로 바꾼다.
	 */
	@Transactional
	public void updatePost(Long memberId, Long postId, PostSaveRequest request) {
		Post post = getWritablePost(memberId, postId);
		SanitizedContent content = writeSupport.sanitizeContent(request.content(),
			ArticleWriteSupport.POST_IMAGE_KEY_PREFIX, post);

		post.update(request.category(), request.title(), content.html());
		post.clearImages();
		content.imageKeys().forEach(post::addImage);
	}

	@Transactional
	public void deletePost(Long memberId, Long postId) {
		getWritablePost(memberId, postId).delete();
	}

	/**
	 * 요청한 페이지를 최신순으로, 크기는 최대 100으로 맞춘다.
	 */
	static Pageable latest(Pageable pageable) {
		return PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), MAX_PAGE_SIZE), LATEST);
	}

	private Post getActivePost(Long postId) {
		return postRepository.findByIdAndDeletedAtIsNull(postId)
			.orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
	}

	/**
	 * 수정·삭제할 수 있는 게시물: 직원 권한이 있는 작성자 본인이거나 관리자.
	 * 권한을 먼저 확인해 직원이 아니면 게시물을 조회하지 않는다.
	 */
	private Post getWritablePost(Long memberId, Long postId) {
		Member member = writeSupport.getWriter(memberId);
		Post post = getActivePost(postId);
		writeSupport.checkWritable(member, post);
		return post;
	}
}
