package com.mongkids.domain.post.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.UnaryOperator;

import com.mongkids.domain.post.entity.Post;
import com.mongkids.domain.post.entity.PostCategory;

public record PostDetailResponse(
	Long id,
	PostCategory category,
	String title,
	String content,
	List<PostImageResponse> images,
	Long authorId,
	String authorName,
	int likeCount,
	int commentCount,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {

	private static final String WITHDRAWN_AUTHOR_NAME = "탈퇴 회원";

	public static PostDetailResponse of(Post post, UnaryOperator<String> imageUrlResolver) {
		List<PostImageResponse> images = post.getImages().stream()
			.map(image -> new PostImageResponse(image.getImageKey(), imageUrlResolver.apply(image.getImageKey())))
			.toList();
		return new PostDetailResponse(
			post.getId(),
			post.getCategory(),
			post.getTitle(),
			post.getContent(),
			images,
			post.getMember().getId(),
			authorName(post),
			post.getLikeCount(),
			post.getCommentCount(),
			post.getCreatedAt(),
			post.getUpdatedAt()
		);
	}

	static String authorName(Post post) {
		return post.getMember().isWithdrawn() ? WITHDRAWN_AUTHOR_NAME : post.getMember().getName();
	}

	/**
	 * 수정 화면에서 기존 사진을 유지하려면 {@code imageKey}를 그대로 다시 보낸다.
	 */
	public record PostImageResponse(String imageKey, String url) {
	}
}
