package com.mongkids.domain.review.dto;

import java.time.LocalDateTime;
import java.util.function.UnaryOperator;

import com.mongkids.domain.review.entity.Review;

/**
 * 관리자 후기 목록 한 줄 (FR-021). 후기 내용은 공개 응답과 같고(작성자 마스킹 포함, FR-014) 숨김 여부만 더한다.
 */
public record AdminReviewResponse(
	ReviewResponse review,
	boolean hidden,
	LocalDateTime hiddenAt
) {

	public static AdminReviewResponse of(Review review, UnaryOperator<String> imageUrlResolver) {
		return new AdminReviewResponse(ReviewResponse.of(review, imageUrlResolver), review.isHidden(),
			review.getHiddenAt());
	}
}
