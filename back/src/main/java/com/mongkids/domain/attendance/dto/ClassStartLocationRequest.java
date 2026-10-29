package com.mongkids.domain.attendance.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * 위치 기반 수업 시작 확인 요청 (EMP-FR-001). 기기가 알려 준 위치 정확도는 판정에 쓰지 않고 저장만 한다.
 */
public record ClassStartLocationRequest(
	@NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
	@NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude,
	@PositiveOrZero Double accuracy
) {
}
