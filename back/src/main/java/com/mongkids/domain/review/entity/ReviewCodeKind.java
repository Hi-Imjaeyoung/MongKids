package com.mongkids.domain.review.entity;

/**
 * 후기 작성 코드 종류 (FR-029, ADR-005). 1회용은 1:1 고객에게, 묶음은 기관을 통해 보호자에게 나눠 준다.
 */
public enum ReviewCodeKind {
	SINGLE,
	BUNDLE
}
