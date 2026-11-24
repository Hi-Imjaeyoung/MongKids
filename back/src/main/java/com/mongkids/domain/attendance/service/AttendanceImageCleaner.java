package com.mongkids.domain.attendance.service;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.mongkids.global.storage.FileStorage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 지운 수업 사진의 S3 객체를 커밋 뒤에 지운다 (EMP-FR-012, 사용자 결정 2026-11-24).
 * DB 삭제가 롤백되면 실행되지 않는다. 지우지 못한 객체는 로그만 남기고 다시 시도하지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceImageCleaner {

	private final FileStorage fileStorage;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onImagesDeleted(AttendanceImagesDeletedEvent event) {
		if (event.imageKeys().isEmpty()) {
			return;
		}
		try {
			List<String> failed = fileStorage.delete(event.imageKeys());
			if (!failed.isEmpty()) {
				log.warn("수업 사진 {}장을 S3에서 지우지 못했습니다: {}", failed.size(), failed);
			}
		} catch (RuntimeException e) {
			log.warn("수업 사진 {}장을 S3에서 지우지 못했습니다: {}", event.imageKeys().size(), event.imageKeys(), e);
		}
	}
}
