package com.mongkids.domain.consultation.service;

/**
 * 신규(RECEIVED) 상담 건수가 바뀌었을 수 있다는 신호 (FR-005).
 * 신청자 정보는 담지 않는다. 받는 쪽이 커밋 뒤에 건수를 다시 센다.
 */
public record NewConsultationCountChangedEvent() {
}
