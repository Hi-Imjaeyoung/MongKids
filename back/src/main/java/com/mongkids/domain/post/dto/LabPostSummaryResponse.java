package com.mongkids.domain.post.dto;

import java.time.LocalDateTime;
import java.util.function.UnaryOperator;

import com.mongkids.domain.post.entity.LabCategory;
import com.mongkids.domain.post.entity.LabPost;

/**
 * 연구소 글 목록 항목. 대표 이미지(0번)만 내려준다.
 */
public record LabPostSummaryResponse(
	Long id,
	LabCategory category,
	String title,
	String thumbnailUrl,
	String authorName,
	int likeCount,
	LocalDateTime createdAt
) {

	public static LabPostSummaryResponse of(LabPost labPost, UnaryOperator<String> imageUrlResolver) {
		String thumbnailUrl = labPost.getImages().isEmpty()
			? null
			: imageUrlResolver.apply(labPost.getImages().get(0).getImageKey());
		return new LabPostSummaryResponse(
			labPost.getId(),
			labPost.getCategory(),
			labPost.getTitle(),
			thumbnailUrl,
			PostDetailResponse.authorName(labPost),
			labPost.getLikeCount(),
			labPost.getCreatedAt()
		);
	}
}
