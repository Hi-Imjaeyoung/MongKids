package com.mongkids.domain.content.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 월별 커리큘럼 저장 요청 (FR-034). 3줄을 순서대로 한 번에 보낸다.
 */
public record AboutCurriculumSaveRequest(
	@NotNull @Size(min = 3, max = 3) List<@NotNull @Valid AboutCurriculumMonthRequest> months
) {
}
