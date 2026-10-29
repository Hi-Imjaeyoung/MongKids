package com.mongkids.domain.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 관리자 반려 사유·직접 등록 사유 (EMP-FR-011).
 */
public record ClassStartReasonRequest(
	@NotBlank @Size(max = 200) String reason
) {
}
