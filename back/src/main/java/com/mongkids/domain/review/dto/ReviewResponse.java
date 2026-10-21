package com.mongkids.domain.review.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.UnaryOperator;

import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.review.entity.Review;
import com.mongkids.domain.review.entity.ReviewImage;
import com.mongkids.domain.review.entity.ReviewType;

/**
 * 후기 목록 항목. 작성자 이름은 첫 글자만 남기고 가린다 (FR-014).
 */
public record ReviewResponse(
	Long id,
	ReviewType type,
	int satisfaction,
	String content,
	Long authorId,
	String authorName,
	List<Image> images,
	Reply reply,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {

	private static final String WITHDRAWN_AUTHOR_NAME = "탈퇴 회원";

	/**
	 * 후기 사진. 수정할 때 남길 사진은 {@code imageKey}를 그대로 다시 보낸다. 첫 사진이 대표 사진이다.
	 */
	public record Image(
		String imageKey,
		String url
	) {
	}

	/**
	 * 관리자 답글 (FR-017). 답글이 없으면 null.
	 */
	public record Reply(
		String content,
		LocalDateTime repliedAt
	) {
	}

	public static ReviewResponse of(Review review, UnaryOperator<String> imageUrlResolver) {
		Reply reply = review.getReplyContent() == null
			? null
			: new Reply(review.getReplyContent(), review.getRepliedAt());
		return new ReviewResponse(
			review.getId(),
			review.getType(),
			review.getSatisfaction(),
			review.getContent(),
			review.getMember().getId(),
			authorName(review.getMember()),
			review.getImages().stream()
				.map(ReviewImage::getImageKey)
				.map(imageKey -> new Image(imageKey, imageUrlResolver.apply(imageKey)))
				.toList(),
			reply,
			review.getCreatedAt(),
			review.getUpdatedAt()
		);
	}

	static String authorName(Member member) {
		return member.isWithdrawn() ? WITHDRAWN_AUTHOR_NAME : maskName(member.getName());
	}

	/**
	 * 첫 글자만 남기고 나머지를 *로 가린다. 홍길동 → 홍**, 김철 → 김*, 한 글자 이름은 *.
	 */
	static String maskName(String name) {
		int[] codePoints = name.strip().codePoints().toArray();
		if (codePoints.length <= 1) {
			return "*";
		}
		return new String(codePoints, 0, 1) + "*".repeat(codePoints.length - 1);
	}
}
