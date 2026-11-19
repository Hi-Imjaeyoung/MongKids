package com.mongkids.domain.content.dto;

import com.mongkids.domain.content.entity.AboutCurriculumMonth;

public record AboutCurriculumMonthResponse(
	String month,
	String topic,
	String goal
) {

	public static AboutCurriculumMonthResponse from(AboutCurriculumMonth row) {
		return new AboutCurriculumMonthResponse(row.getMonth(), row.getTopic(), row.getGoal());
	}
}
