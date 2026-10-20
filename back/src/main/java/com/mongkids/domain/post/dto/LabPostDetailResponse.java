package com.mongkids.domain.post.dto;

import java.time.LocalDateTime;

import com.mongkids.domain.post.entity.LabCategory;
import com.mongkids.domain.post.entity.LabPost;

public record LabPostDetailResponse(
	Long id,
	LabCategory category,
	String title,
	String content,
	Long authorId,
	String authorName,
	int likeCount,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {

	/**
	 * @param content 응답용으로 바꾼 본문 HTML (사진 src 채움)
	 */
	public static LabPostDetailResponse of(LabPost labPost, String content) {
		return new LabPostDetailResponse(
			labPost.getId(),
			labPost.getCategory(),
			labPost.getTitle(),
			content,
			labPost.getMember().getId(),
			PostDetailResponse.authorName(labPost),
			labPost.getLikeCount(),
			labPost.getCreatedAt(),
			labPost.getUpdatedAt()
		);
	}
}
