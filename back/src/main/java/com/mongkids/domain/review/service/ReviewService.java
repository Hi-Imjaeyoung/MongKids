package com.mongkids.domain.review.service;

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
import com.mongkids.domain.review.dto.ReviewImageUploadUrlRequest;
import com.mongkids.domain.review.dto.ReviewImageUploadUrlResponse;
import com.mongkids.domain.review.dto.ReviewResponse;
import com.mongkids.domain.review.dto.ReviewSaveRequest;
import com.mongkids.domain.review.entity.Review;
import com.mongkids.domain.review.entity.ReviewType;
import com.mongkids.domain.review.repository.ReviewRepository;
import com.mongkids.global.common.PageResponse;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;
import com.mongkids.global.storage.FileStorage;
import com.mongkids.global.storage.PresignedUpload;

import lombok.RequiredArgsConstructor;

/**
 * 수업 후기 (FR-013~016, FR-025). 로그인한 회원이면 누구나 쓸 수 있고, 저장하면 바로 공개된다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewService {

	static final int MAX_PAGE_SIZE = 100;
	static final int GUEST_PREVIEW_SIZE = 4;
	static final int MAX_IMAGE_COUNT = 5;
	static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;
	static final String IMAGE_KEY_PREFIX = "reviews/";
	static final Sort LATEST = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

	private static final Map<String, String> IMAGE_EXTENSIONS = Map.of(
		"image/jpeg", "jpg",
		"image/png", "png",
		"image/webp", "webp"
	);

	private final ReviewRepository reviewRepository;
	private final MemberService memberService;
	private final FileStorage fileStorage;

	/**
	 * 후기 사진을 S3에 직접 올릴 URL을 발급한다 (FR-015: 최대 5장, 장당 10MB).
	 */
	public List<ReviewImageUploadUrlResponse> createImageUploadUrls(Long memberId,
		ReviewImageUploadUrlRequest request) {
		memberService.getActiveMember(memberId);
		validateImageCount(request.files().size());
		return request.files().stream()
			.map(file -> {
				String extension = IMAGE_EXTENSIONS.get(file.contentType());
				if (extension == null || file.size() <= 0 || file.size() > MAX_IMAGE_SIZE) {
					throw new BusinessException(ErrorCode.INVALID_REVIEW_IMAGE);
				}
				String imageKey = newImageKey(extension);
				PresignedUpload upload = fileStorage.createUploadUrl(imageKey, file.contentType(), file.size());
				return new ReviewImageUploadUrlResponse(imageKey, upload.uploadUrl());
			})
			.toList();
	}

	@Transactional
	public ReviewResponse createReview(Long memberId, ReviewSaveRequest request) {
		Member member = memberService.getActiveMember(memberId);
		List<String> imageKeys = request.imageKeysOrEmpty();
		validateImageKeys(imageKeys);

		Review review = Review.builder()
			.member(member)
			.type(request.type())
			.satisfaction(request.satisfaction())
			.content(request.content())
			.build();
		imageKeys.forEach(review::addImage);
		return toResponse(reviewRepository.save(review));
	}

	/**
	 * 최신순 목록. 유형이 없으면 전체 탭이다.
	 * 비로그인이면 요청한 페이지와 상관없이 최신 4개만 주고 다음 페이지는 없다고 알린다 (FR-012).
	 * 이때도 {@code totalElements}는 실제 건수라 화면이 로그인 안내를 띄울지 판단할 수 있다.
	 */
	public PageResponse<ReviewResponse> getReviews(Long memberId, ReviewType type, Pageable pageable) {
		if (memberId == null) {
			Page<Review> preview = findVisible(type, PageRequest.of(0, GUEST_PREVIEW_SIZE, LATEST));
			List<ReviewResponse> content = preview.getContent().stream().map(this::toResponse).toList();
			return new PageResponse<>(content, 0, GUEST_PREVIEW_SIZE, preview.getTotalElements(),
				content.isEmpty() ? 0 : 1, false);
		}
		Pageable latest = PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), MAX_PAGE_SIZE),
			LATEST);
		return PageResponse.from(findVisible(type, latest), this::toResponse);
	}

	/**
	 * 후기를 수정한다. 사진 목록은 요청의 사진으로 통째로 바꾼다.
	 */
	@Transactional
	public ReviewResponse updateReview(Long memberId, Long reviewId, ReviewSaveRequest request) {
		Review review = getOwnReview(memberId, reviewId);
		List<String> imageKeys = request.imageKeysOrEmpty();
		validateImageKeys(imageKeys);

		review.update(request.type(), request.satisfaction(), request.content());
		review.clearImages();
		imageKeys.forEach(review::addImage);
		return toResponse(review);
	}

	@Transactional
	public void deleteReview(Long memberId, Long reviewId) {
		getOwnReview(memberId, reviewId).delete();
	}

	private Page<Review> findVisible(ReviewType type, Pageable pageable) {
		return type == null
			? reviewRepository.findByDeletedAtIsNullAndHiddenAtIsNull(pageable)
			: reviewRepository.findByTypeAndDeletedAtIsNullAndHiddenAtIsNull(type, pageable);
	}

	/**
	 * 작성자 본인의 후기. 관리자도 남의 후기는 고치거나 지울 수 없고 숨김(FR-021)으로 처리한다 (FR-025).
	 */
	private Review getOwnReview(Long memberId, Long reviewId) {
		Member member = memberService.getActiveMember(memberId);
		Review review = reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
			.orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
		if (!review.getMember().getId().equals(member.getId())) {
			throw new BusinessException(ErrorCode.FORBIDDEN);
		}
		return review;
	}

	private ReviewResponse toResponse(Review review) {
		return ReviewResponse.of(review, fileStorage::getUrl);
	}

	private void validateImageCount(int count) {
		if (count > MAX_IMAGE_COUNT) {
			throw new BusinessException(ErrorCode.REVIEW_IMAGE_LIMIT_EXCEEDED);
		}
	}

	/**
	 * 업로드 URL로 실제 올라간 후기 사진인지 확인한다.
	 */
	private void validateImageKeys(List<String> imageKeys) {
		validateImageCount(imageKeys.size());
		if (new HashSet<>(imageKeys).size() != imageKeys.size()) {
			throw new BusinessException(ErrorCode.INVALID_REVIEW_IMAGE);
		}
		for (String imageKey : imageKeys) {
			if (!imageKey.startsWith(IMAGE_KEY_PREFIX)) {
				throw new BusinessException(ErrorCode.INVALID_REVIEW_IMAGE);
			}
			long size = fileStorage.findSize(imageKey)
				.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REVIEW_IMAGE));
			if (size > MAX_IMAGE_SIZE) {
				throw new BusinessException(ErrorCode.INVALID_REVIEW_IMAGE);
			}
		}
	}

	private static String newImageKey(String extension) {
		YearMonth now = YearMonth.now();
		return "%s%d/%02d/%s.%s".formatted(IMAGE_KEY_PREFIX, now.getYear(), now.getMonthValue(), UUID.randomUUID(),
			extension);
	}
}
