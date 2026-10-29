package com.mongkids.domain.attendance.dto;

/**
 * 수업 사진 업로드 URL. 클라이언트는 {@code uploadUrl}로 사진을 PUT 한 뒤 수업 종료 요청에 {@code imageKey}를 보낸다.
 */
public record ClassEndImageUploadUrlResponse(
	String imageKey,
	String uploadUrl
) {
}
