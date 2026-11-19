package com.mongkids.domain.classplan.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 계획안 PDF 업로드 URL 발급 요청. 형식·크기는 Service에서 검증한다.
 * target: PUBLIC(누구나 보는 PDF: 연간 계획안·월간 블러본), ORIGINAL(월간 원본)
 */
public record ClassPlanFileUploadUrlRequest(
	@NotEmpty @Size(max = 2) @Valid List<PlanFile> files
) {

	public record PlanFile(
		@NotNull Target target,
		@NotBlank String contentType,
		long size
	) {
	}

	public enum Target {
		PUBLIC,
		ORIGINAL
	}
}
