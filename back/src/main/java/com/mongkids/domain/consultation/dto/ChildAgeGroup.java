package com.mongkids.domain.consultation.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 기관 상담 신청 폼의 원아 연령 선택지. 저장할 때는 라벨을 합쳐 문자열로 둔다 (child_age_range).
@Getter
@RequiredArgsConstructor
public enum ChildAgeGroup {

	UNDER_2("만 2세 이하"),
	AGE_3("만 3세"),
	AGE_4("만 4세"),
	AGE_5("만 5세"),
	OVER_6("만 6세 이상");

	private final String label;
}
