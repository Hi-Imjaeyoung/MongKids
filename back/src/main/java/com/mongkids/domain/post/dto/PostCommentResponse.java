package com.mongkids.domain.post.dto;

import java.time.LocalDateTime;

import com.mongkids.domain.post.entity.PostComment;

public record PostCommentResponse(
	Long id,
	String content,
	Long authorId,
	String authorName,
	LocalDateTime createdAt
) {

	public static PostCommentResponse from(PostComment comment) {
		return new PostCommentResponse(
			comment.getId(),
			comment.getContent(),
			comment.getMember().getId(),
			PostDetailResponse.authorName(comment.getMember()),
			comment.getCreatedAt()
		);
	}
}
