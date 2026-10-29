package com.mongkids.domain.attendance.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * 수업 시작 수동 등록 요청 (EMP-FR-011). 위치를 받았다면 함께 보내고, 못 받았으면 위치 없이 보낸다.
 */
public record ClassStartManualRequest(
	@NotBlank @Size(max = 200) String reason,
	@DecimalMin("-90") @DecimalMax("90") Double latitude,
	@DecimalMin("-180") @DecimalMax("180") Double longitude,
	@PositiveOrZero Double accuracy
) {

	public boolean hasLocation() {
		return latitude != null && longitude != null;
	}
}
