package com.mongkids.domain.review.dto;

import java.time.LocalDate;

import com.mongkids.domain.review.entity.ReviewCodeKind;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 후기 작성 코드 발급 요청 (FR-029). 사용 횟수와 메모는 묶음 코드만 받고, 1회용은 사용 횟수 1·메모 없음으로 저장한다.
 */
public record ReviewCodeCreateRequest(
	@NotNull ReviewCodeKind kind,
	@Min(1) @Max(500) Integer maxUses,
	@NotNull LocalDate expiresOn,
	@Size(max = 50) String memo
) {
}
