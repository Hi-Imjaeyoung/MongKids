package com.mongkids.domain.consultation.entity;

// 견적 신청(FR-031)의 행사 장소 구분. 실내·실외는 행사 주소가 필수다
public enum VenueType {
	INDOOR,
	OUTDOOR,
	UNDECIDED;

	public boolean needsAddress() {
		return this != UNDECIDED;
	}
}
