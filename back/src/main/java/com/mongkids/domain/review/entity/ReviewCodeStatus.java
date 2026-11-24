package com.mongkids.domain.review.entity;

/**
 * 후기 작성 코드 상태 (FR-029). 저장하지 않고 폐기 여부·사용 횟수·만료일과 오늘 날짜로 계산한다.
 * 폐기가 가장 앞서고, 그다음 다 씀, 만료 순이다.
 */
public enum ReviewCodeStatus {
	ACTIVE,
	USED_UP,
	EXPIRED,
	REVOKED
}
