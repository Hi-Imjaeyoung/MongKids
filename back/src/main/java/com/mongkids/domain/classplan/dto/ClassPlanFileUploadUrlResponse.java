package com.mongkids.domain.classplan.dto;

/**
 * 계획안 PDF 업로드 URL. 클라이언트는 {@code uploadUrl}로 PDF를 PUT 한 뒤 등록·수정 요청에 {@code fileKey}를 보낸다.
 */
public record ClassPlanFileUploadUrlResponse(
	String fileKey,
	String uploadUrl
) {
}
