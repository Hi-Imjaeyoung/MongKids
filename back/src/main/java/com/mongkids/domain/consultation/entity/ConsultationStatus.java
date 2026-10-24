package com.mongkids.domain.consultation.entity;

/**
 * 상담 처리 상태 (FR-026). 관리자가 어느 단계로든 바꿀 수 있다.
 */
public enum ConsultationStatus {
	RECEIVED,
	CONTACTED,
	COMPLETED
}
