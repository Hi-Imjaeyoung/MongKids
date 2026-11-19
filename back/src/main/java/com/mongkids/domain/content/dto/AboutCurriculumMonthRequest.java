package com.mongkids.domain.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 월별 커리큘럼 한 줄 (FR-034). 길이 제한은 화면의 월 칸 너비 기준이다.
 */
public record AboutCurriculumMonthRequest(
	@NotBlank @Size(max = 10) String month,
	@NotBlank @Size(max = 40) String topic,
	@NotBlank @Size(max = 40) String goal
) {
}
