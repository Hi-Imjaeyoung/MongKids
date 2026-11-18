package com.mongkids.domain.attendance.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.mongkids.domain.attendance.dto.ClassStatus;
import com.mongkids.domain.attendance.dto.DayStatus;
import com.mongkids.domain.attendance.entity.DayEnd;
import com.mongkids.domain.attendance.entity.DayStart;
import com.mongkids.domain.attendance.entity.ClassEnd;
import com.mongkids.domain.attendance.entity.ClassStart;
import com.mongkids.domain.attendance.entity.ClassStartStatus;

class AttendanceServiceTest {

	@Test
	@DisplayName("두 좌표 사이 거리를 m 단위로 계산한다 (위도 0.001도는 약 111m)")
	void distanceMeters() {
		assertThat(AttendanceService.distanceMeters(37.5, 127.0, 37.5, 127.0)).isZero();
		assertThat(AttendanceService.distanceMeters(37.5, 127.0, 37.501, 127.0)).isBetween(110, 112);
		assertThat(AttendanceService.distanceMeters(37.5, 127.0, 37.5, 127.001)).isBetween(87, 90);
	}

	@Test
	@DisplayName("수업 상태: 기록 없음은 시작 전, 반려는 종료 기록이 있어도 반려, 확정·승인 대기에 종료 기록이 있으면 종료 완료")
	void classStatus() {
		ClassEnd end = mock(ClassEnd.class);

		assertThat(ClassStatus.of(null, null)).isEqualTo(ClassStatus.NOT_STARTED);
		assertThat(ClassStatus.of(start(ClassStartStatus.CONFIRMED), null)).isEqualTo(ClassStatus.STARTED);
		assertThat(ClassStatus.of(start(ClassStartStatus.PENDING), null)).isEqualTo(ClassStatus.PENDING);
		assertThat(ClassStatus.of(start(ClassStartStatus.PENDING), end)).isEqualTo(ClassStatus.ENDED);
		assertThat(ClassStatus.of(start(ClassStartStatus.CONFIRMED), end)).isEqualTo(ClassStatus.ENDED);
		assertThat(ClassStatus.of(start(ClassStartStatus.REJECTED), null)).isEqualTo(ClassStatus.REJECTED);
		assertThat(ClassStatus.of(start(ClassStartStatus.REJECTED), end)).isEqualTo(ClassStatus.REJECTED);
	}

	@Test
	@DisplayName("하루 상태: 시작 반려는 종료가 있어도 반려, 종료 기록이 있으면 종료의 승인 상태를 따른다")
	void dayStatus() {
		assertThat(DayStatus.of(null, null)).isEqualTo(DayStatus.NOT_STARTED);
		assertThat(DayStatus.of(dayStart(ClassStartStatus.CONFIRMED), null)).isEqualTo(DayStatus.STARTED);
		assertThat(DayStatus.of(dayStart(ClassStartStatus.PENDING), null)).isEqualTo(DayStatus.PENDING);
		assertThat(DayStatus.of(dayStart(ClassStartStatus.REJECTED), dayEnd(ClassStartStatus.CONFIRMED)))
			.isEqualTo(DayStatus.REJECTED);
		assertThat(DayStatus.of(dayStart(ClassStartStatus.PENDING), dayEnd(ClassStartStatus.CONFIRMED)))
			.isEqualTo(DayStatus.ENDED);
		assertThat(DayStatus.of(dayStart(ClassStartStatus.CONFIRMED), dayEnd(ClassStartStatus.PENDING)))
			.isEqualTo(DayStatus.END_PENDING);
		assertThat(DayStatus.of(dayStart(ClassStartStatus.CONFIRMED), dayEnd(ClassStartStatus.REJECTED)))
			.isEqualTo(DayStatus.END_REJECTED);
	}

	private static DayStart dayStart(ClassStartStatus status) {
		DayStart start = DayStart.of(null, null);
		ReflectionTestUtils.setField(start, "status", status);
		return start;
	}

	private static DayEnd dayEnd(ClassStartStatus status) {
		DayEnd end = DayEnd.of(null, null);
		ReflectionTestUtils.setField(end, "status", status);
		return end;
	}

	private static ClassStart start(ClassStartStatus status) {
		ClassStart start = ClassStart.of(null);
		ReflectionTestUtils.setField(start, "status", status);
		return start;
	}
}
