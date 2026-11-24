package com.mongkids.domain.attendance.service;

import java.util.List;

/**
 * 수업 사진 행을 지웠다는 신호 (기관 삭제, EMP-FR-012). 커밋된 뒤에 S3 객체를 지운다.
 * 다른 기록이 아직 쓰는 키는 담지 않는다.
 */
public record AttendanceImagesDeletedEvent(List<String> imageKeys) {
}
