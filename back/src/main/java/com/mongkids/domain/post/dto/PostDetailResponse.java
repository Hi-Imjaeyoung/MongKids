package com.mongkids.domain.post.dto;

import java.time.LocalDateTime;

import com.mongkids.domain.post.entity.Article;
import com.mongkids.domain.post.entity.Post;
import com.mongkids.domain.post.entity.PostCategory;

public record PostDetailResponse(
	Long id,
	PostCategory category,
	String title,
	String content,
	Long authorId,
	String authorName,
	int likeCount,
	int commentCount,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {

	private static final String WITHDRAWN_AUTHOR_NAME = "탈퇴 회원";

	/**
	 * @param content 응답용으로 바꾼 본문 HTML (사진 src 채움)
	 */
	public static PostDetailResponse of(Post post, String content) {
		return new PostDetailResponse(
			post.getId(),
			post.getCategory(),
			post.getTitle(),
			content,
			post.getMember().getId(),
			authorName(post),
			post.getLikeCount(),
			post.getCommentCount(),
			post.getCreatedAt(),
			post.getUpdatedAt()
		);
	}

	static String authorName(Article article) {
		return article.getMember().isWithdrawn() ? WITHDRAWN_AUTHOR_NAME : article.getMember().getName();
	}
}
