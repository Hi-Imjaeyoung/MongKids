package com.mongkids.domain.classplan.entity;

/**
 * 수업 계획안 종류 (SCR-027). 연간은 누구나 보고, 월간 원본은 인증된 사람만 본다(FR-032).
 */
public enum ClassPlanKind {
	ANNUAL,
	MONTHLY;

	/** 원본 PDF를 따로 두는 종류인지. 월간은 공개 블러본과 비공개 원본 두 파일이다 (ADR-009) */
	public boolean hasOriginal() {
		return this == MONTHLY;
	}
}
