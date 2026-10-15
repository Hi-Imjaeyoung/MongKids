package com.mongkids.domain.post.dto;

/**
 * 사진 업로드 URL. 클라이언트는 {@code uploadUrl}로 사진을 PUT 한 뒤 게시물 저장 요청에 {@code imageKey}를 보낸다.
 */
public record PostImageUploadUrlResponse(
	String imageKey,
	String uploadUrl
) {
}
