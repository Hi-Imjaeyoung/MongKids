package com.mongkids.domain.post.dto;

import java.time.LocalDateTime;
import java.util.function.UnaryOperator;

import com.mongkids.domain.post.entity.Post;
import com.mongkids.domain.post.entity.PostCategory;

/**
 * 게시물 목록 항목. 대표 이미지(0번)만 내려준다.
 */
public record PostSummaryResponse(
	Long id,
	PostCategory category,
	String title,
	String thumbnailUrl,
	String authorName,
	int likeCount,
	int commentCount,
	LocalDateTime createdAt
) {

	public static PostSummaryResponse of(Post post, UnaryOperator<String> imageUrlResolver) {
		String thumbnailUrl = post.getImages().isEmpty()
			? null
			: imageUrlResolver.apply(post.getImages().get(0).getImageKey());
		return new PostSummaryResponse(
			post.getId(),
			post.getCategory(),
			post.getTitle(),
			thumbnailUrl,
			PostDetailResponse.authorName(post),
			post.getLikeCount(),
			post.getCommentCount(),
			post.getCreatedAt()
		);
	}
}
