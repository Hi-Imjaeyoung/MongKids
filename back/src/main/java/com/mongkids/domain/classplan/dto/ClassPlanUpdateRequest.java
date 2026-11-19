package com.mongkids.domain.classplan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 계획안 수정. 종류는 바꾸지 않는다. 파일 키가 없으면 그 파일은 그대로 둔다.
 */
public record ClassPlanUpdateRequest(
	@NotBlank @Size(max = 50) String title,
	@NotBlank @Size(max = 100) String period,
	String fileKey,
	String originalFileKey
) {
}
