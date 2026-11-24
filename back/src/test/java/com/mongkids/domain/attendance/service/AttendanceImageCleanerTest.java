package com.mongkids.domain.attendance.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mongkids.global.storage.FileStorage;

class AttendanceImageCleanerTest {

	private final FileStorage fileStorage = mock(FileStorage.class);
	private final AttendanceImageCleaner cleaner = new AttendanceImageCleaner(fileStorage);

	@Test
	@DisplayName("지운 사진 키를 S3에서 한꺼번에 지운다")
	void deletesKeys() {
		// given
		List<String> keys = List.of("attendance/2026/11/0.jpg", "attendance/2026/11/1.jpg");
		when(fileStorage.delete(keys)).thenReturn(List.of());

		// when
		cleaner.onImagesDeleted(new AttendanceImagesDeletedEvent(keys));

		// then
		verify(fileStorage).delete(keys);
	}

	@Test
	@DisplayName("지울 키가 없으면 S3를 부르지 않는다")
	void skipsEmpty() {
		cleaner.onImagesDeleted(new AttendanceImagesDeletedEvent(List.of()));

		verifyNoInteractions(fileStorage);
	}

	@Test
	@DisplayName("S3 삭제가 실패해도 예외를 밖으로 던지지 않는다 (기관 삭제는 이미 커밋됨, 로그만 남김)")
	void swallowsFailure() {
		// given
		List<String> keys = List.of("attendance/2026/11/0.jpg");
		when(fileStorage.delete(keys)).thenThrow(new IllegalStateException("AccessDenied"));

		// when, then
		assertThatCode(() -> cleaner.onImagesDeleted(new AttendanceImagesDeletedEvent(keys)))
			.doesNotThrowAnyException();
	}
}
