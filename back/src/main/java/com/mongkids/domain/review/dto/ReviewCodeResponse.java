package com.mongkids.domain.review.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.mongkids.domain.review.entity.ReviewCode;
import com.mongkids.domain.review.entity.ReviewCodeKind;
import com.mongkids.domain.review.entity.ReviewCodeStatus;

/**
 * 관리자 후기 작성 코드 목록 한 줄 (FR-029). status는 오늘 날짜 기준으로 계산한다.
 */
public record ReviewCodeResponse(
	Long id,
	String code,
	ReviewCodeKind kind,
	int maxUses,
	int usedCount,
	LocalDate expiresOn,
	String memo,
	ReviewCodeStatus status,
	LocalDateTime revokedAt,
	LocalDateTime createdAt
) {

	public static ReviewCodeResponse of(ReviewCode reviewCode, LocalDate today) {
		return new ReviewCodeResponse(reviewCode.getId(), reviewCode.getCode(), reviewCode.getKind(),
			reviewCode.getMaxUses(), reviewCode.getUsedCount(), reviewCode.getExpiresOn(), reviewCode.getMemo(),
			reviewCode.status(today), reviewCode.getRevokedAt(), reviewCode.getCreatedAt());
	}
}
