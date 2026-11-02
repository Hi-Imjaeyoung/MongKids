package com.mongkids.domain.attendance.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 기관 등록·수정 요청 (EMP-FR-012). 좌표는 관리자가 지도에서 확인한 값이다.
 */
public record InstitutionRequest(
	@NotBlank @Size(max = 100) String name,
	@NotBlank @Size(max = 255) String address,
	@NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
	@NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude
) {
}
