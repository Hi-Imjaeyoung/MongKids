package com.mongkids.domain.post.dto;

/**
 * 공감 상태 (FR-011). {@code liked}는 요청한 회원이 공감했는지이며 비로그인이면 false다.
 */
public record LikeResponse(boolean liked, int likeCount) {
}
