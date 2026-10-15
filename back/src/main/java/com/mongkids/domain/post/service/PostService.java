package com.mongkids.domain.post.service;

import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.domain.post.dto.PostCreateResponse;
import com.mongkids.domain.post.dto.PostDetailResponse;
import com.mongkids.domain.post.dto.PostImageUploadUrlRequest;
import com.mongkids.domain.post.dto.PostImageUploadUrlResponse;
import com.mongkids.domain.post.dto.PostSaveRequest;
import com.mongkids.domain.post.dto.PostSummaryResponse;
import com.mongkids.domain.post.entity.Post;
import com.mongkids.domain.post.entity.PostCategory;
import com.mongkids.domain.post.repository.PostRepository;
import com.mongkids.global.common.PageResponse;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;
import com.mongkids.global.storage.FileStorage;
import com.mongkids.global.storage.PresignedUpload;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

	static final int MAX_IMAGE_COUNT = 20;
	static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;
	static final int MAX_PAGE_SIZE = 100;

	private static final String IMAGE_KEY_PREFIX = "posts/";
	private static final Map<String, String> IMAGE_EXTENSIONS = Map.of(
		"image/jpeg", "jpg",
		"image/png", "png",
		"image/webp", "webp"
	);
	private static final Sort LATEST = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

	private final PostRepository postRepository;
	private final MemberService memberService;
	private final FileStorage fileStorage;

	/**
	 * 사진을 S3에 직접 올릴 URL을 발급한다 (FR-008: 최대 20장, 장당 10MB).
	 */
	public List<PostImageUploadUrlResponse> createImageUploadUrls(PostImageUploadUrlRequest request) {
		validateImageCount(request.files().size());
		return request.files().stream()
			.map(file -> {
				String extension = IMAGE_EXTENSIONS.get(file.contentType());
				if (extension == null || file.size() <= 0 || file.size() > MAX_IMAGE_SIZE) {
					throw new BusinessException(ErrorCode.INVALID_POST_IMAGE);
				}
				String imageKey = newImageKey(extension);
				PresignedUpload upload = fileStorage.createUploadUrl(imageKey, file.contentType(), file.size());
				return new PostImageUploadUrlResponse(imageKey, upload.uploadUrl());
			})
			.toList();
	}

	@Transactional
	public PostCreateResponse createPost(Long memberId, PostSaveRequest request) {
		Member member = memberService.getActiveMember(memberId);
		validateImageKeys(request.imageKeys());

		Post post = Post.builder()
			.member(member)
			.category(request.category())
			.title(request.title())
			.content(request.content())
			.build();
		request.imageKeys().forEach(post::addImage);
		return new PostCreateResponse(postRepository.save(post).getId());
	}

	/**
	 * 최신순 목록. 분류가 없으면 전체 탭이다.
	 */
	public PageResponse<PostSummaryResponse> getPosts(PostCategory category, Pageable pageable) {
		Pageable latest = PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), MAX_PAGE_SIZE),
			LATEST);
		Page<Post> posts = category == null
			? postRepository.findByDeletedAtIsNull(latest)
			: postRepository.findByCategoryAndDeletedAtIsNull(category, latest);
		return PageResponse.from(posts, post -> PostSummaryResponse.of(post, fileStorage::getUrl));
	}

	public PostDetailResponse getPost(Long postId) {
		return PostDetailResponse.of(getActivePost(postId), fileStorage::getUrl);
	}

	/**
	 * 게시물을 수정한다. 사진은 요청 목록으로 통째로 바꾼다.
	 */
	@Transactional
	public void updatePost(Long memberId, Long postId, PostSaveRequest request) {
		Post post = getWritablePost(memberId, postId);
		validateImageKeys(request.imageKeys());

		post.update(request.category(), request.title(), request.content());
		post.clearImages();
		request.imageKeys().forEach(post::addImage);
	}

	@Transactional
	public void deletePost(Long memberId, Long postId) {
		getWritablePost(memberId, postId).delete();
	}

	private Post getActivePost(Long postId) {
		return postRepository.findByIdAndDeletedAtIsNull(postId)
			.orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
	}

	private Post getWritablePost(Long memberId, Long postId) {
		Post post = getActivePost(postId);
		if (!post.getMember().getId().equals(memberId)) {
			throw new BusinessException(ErrorCode.FORBIDDEN);
		}
		return post;
	}

	private void validateImageCount(int count) {
		if (count > MAX_IMAGE_COUNT) {
			throw new BusinessException(ErrorCode.POST_IMAGE_LIMIT_EXCEEDED);
		}
	}

	/**
	 * 업로드 URL로 실제 올라간 게시물 사진인지 확인한다.
	 */
	private void validateImageKeys(List<String> imageKeys) {
		validateImageCount(imageKeys.size());
		if (new HashSet<>(imageKeys).size() != imageKeys.size()) {
			throw new BusinessException(ErrorCode.INVALID_POST_IMAGE);
		}
		for (String imageKey : imageKeys) {
			if (!imageKey.startsWith(IMAGE_KEY_PREFIX)) {
				throw new BusinessException(ErrorCode.INVALID_POST_IMAGE);
			}
			long size = fileStorage.findSize(imageKey)
				.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_POST_IMAGE));
			if (size > MAX_IMAGE_SIZE) {
				throw new BusinessException(ErrorCode.INVALID_POST_IMAGE);
			}
		}
	}

	private static String newImageKey(String extension) {
		YearMonth now = YearMonth.now();
		return "%s%d/%02d/%s.%s".formatted(IMAGE_KEY_PREFIX, now.getYear(), now.getMonthValue(), UUID.randomUUID(),
			extension);
	}
}
