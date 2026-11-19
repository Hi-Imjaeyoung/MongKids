package com.mongkids.domain.classplan.dto;

import java.time.LocalDateTime;

import com.mongkids.domain.classplan.entity.ClassPlan;
import com.mongkids.domain.classplan.entity.ClassPlanKind;

/**
 * 수업 계획안 목록 항목. 파일 키는 내보내지 않는다. 공개 PDF는 {@code GET /api/v1/class-plans/{id}/file}로 받고,
 * {@code updatedAt}은 파일을 바꿨을 때 화면이 새로 받도록 주소에 붙인다.
 */
public record ClassPlanResponse(
	Long id,
	ClassPlanKind kind,
	String title,
	String period,
	LocalDateTime updatedAt
) {

	public static ClassPlanResponse from(ClassPlan classPlan) {
		return new ClassPlanResponse(classPlan.getId(), classPlan.getKind(), classPlan.getTitle(),
			classPlan.getPeriod(), classPlan.getUpdatedAt());
	}
}
