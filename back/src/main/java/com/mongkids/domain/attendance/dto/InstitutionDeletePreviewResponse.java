package com.mongkids.domain.attendance.dto;

/**
 * 기관을 지우면 함께 지워질 수 (EMP-FR-012). 관리자가 삭제 확인 창에서 보고 동의한다.
 *
 * @param classSessionCount 이 기관 수업
 * @param classRecordCount 이 기관 수업의 수업 단위 시작·종료 기록 (하루 단위 적용 전)
 * @param workDayCount 이 기관을 첫·마지막 기관으로 대조한 하루 기록. 그날 다른 기관 사진·특이사항도 함께 지운다
 * @param noteCount 남는 하루 기록에 적힌 이 기관 특이사항
 * @param imageCount 함께 지워질 수업 사진
 */
public record InstitutionDeletePreviewResponse(
	long classSessionCount,
	long classRecordCount,
	long workDayCount,
	long noteCount,
	long imageCount
) {
}
