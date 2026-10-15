package com.mongkids.domain.post.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

/**
 * 사진 업로드 URL 발급 요청. 개수·형식·크기 제한은 Service에서 검증한다.
 */
public record PostImageUploadUrlRequest(
	@NotEmpty @Valid List<ImageFile> files
) {

	public record ImageFile(
		@NotBlank String contentType,
		long size
	) {
	}
}
