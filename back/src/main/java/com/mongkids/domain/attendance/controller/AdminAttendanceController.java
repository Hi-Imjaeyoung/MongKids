package com.mongkids.domain.attendance.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongkids.domain.attendance.dto.AdminClassSessionResponse;
import com.mongkids.domain.attendance.dto.AdminWorkDayResponse;
import com.mongkids.domain.attendance.dto.ClassStartReasonRequest;
import com.mongkids.domain.attendance.dto.WorkCalendarResponse;
import com.mongkids.domain.attendance.entity.DayCheckType;
import com.mongkids.domain.attendance.service.AttendanceService;
import com.mongkids.domain.attendance.service.WorkDayService;
import com.mongkids.global.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 근태 관리자 기능 (EMP-FR-003, EMP-FR-011, EMP-FR-017, EMP-FR-020, EMP-SCR-006)
@RestController
@RequestMapping("/api/v1/admin/attendance")
@RequiredArgsConstructor
public class AdminAttendanceController {

	private final AttendanceService attendanceService;
	private final WorkDayService workDayService;

	// 하루 시작·종료 승인 대기 (EMP-FR-020)
	@GetMapping("/pending-days")
	public ApiResponse<List<AdminWorkDayResponse>> getPendingDays(@AuthenticationPrincipal Long memberId) {
		return ApiResponse.success(workDayService.getPendingDays(memberId));
	}

	@GetMapping("/days")
	public ApiResponse<List<AdminWorkDayResponse>> getDays(
		@AuthenticationPrincipal Long memberId,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
		@RequestParam(required = false) Long staffId,
		@RequestParam(required = false) Long institutionId
	) {
		return ApiResponse.success(workDayService.getDays(memberId, from, to, staffId, institutionId));
	}

	// 근무 캘린더: 기간(주·월)별 직원 하루 상태와 수업 수 (EMP-FR-017)
	@GetMapping("/calendar")
	public ApiResponse<WorkCalendarResponse> getCalendar(
		@AuthenticationPrincipal Long memberId,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
		@RequestParam(required = false) Long staffId
	) {
		return ApiResponse.success(workDayService.getCalendar(memberId, from, to, staffId));
	}

	@PostMapping("/staff/{staffId}/days/{date}/{type:start|end}/approve")
	public ApiResponse<AdminWorkDayResponse> approveDay(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long staffId,
		@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
		@PathVariable String type
	) {
		return ApiResponse.success(workDayService.approve(memberId, staffId, date, toType(type)));
	}

	@PostMapping("/staff/{staffId}/days/{date}/{type:start|end}/reject")
	public ApiResponse<AdminWorkDayResponse> rejectDay(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long staffId,
		@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
		@PathVariable String type,
		@Valid @RequestBody ClassStartReasonRequest request
	) {
		return ApiResponse.success(workDayService.reject(memberId, staffId, date, toType(type), request));
	}

	// 관리자 직접 등록. 지난 날짜도 가능
	@PostMapping("/staff/{staffId}/days/{date}/{type:start|end}")
	public ApiResponse<AdminWorkDayResponse> registerDay(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long staffId,
		@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
		@PathVariable String type,
		@Valid @RequestBody ClassStartReasonRequest request
	) {
		return ApiResponse.success(workDayService.registerByAdmin(memberId, staffId, date, toType(type), request));
	}

	// 하루 단위 적용 전 수업 단위 기록의 남은 승인 대기 (EMP-FR-011)
	@GetMapping("/pending-starts")
	public ApiResponse<List<AdminClassSessionResponse>> getPendingStarts(@AuthenticationPrincipal Long memberId) {
		return ApiResponse.success(attendanceService.getPendingStarts(memberId));
	}

	// 수업 목록 (수업 할당 EMP-FR-004)
	@GetMapping("/classes")
	public ApiResponse<List<AdminClassSessionResponse>> getClassRecords(
		@AuthenticationPrincipal Long memberId,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
		@RequestParam(required = false) Long staffId,
		@RequestParam(defaultValue = "false") boolean unassigned,
		@RequestParam(required = false) Long institutionId
	) {
		return ApiResponse.success(
			attendanceService.getClassRecords(memberId, from, to, staffId, unassigned, institutionId));
	}

	@PostMapping("/classes/{classSessionId}/start/approve")
	public ApiResponse<AdminClassSessionResponse> approveStart(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long classSessionId
	) {
		return ApiResponse.success(attendanceService.approveStart(memberId, classSessionId));
	}

	@PostMapping("/classes/{classSessionId}/start/reject")
	public ApiResponse<AdminClassSessionResponse> rejectStart(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long classSessionId,
		@Valid @RequestBody ClassStartReasonRequest request
	) {
		return ApiResponse.success(attendanceService.rejectStart(memberId, classSessionId, request));
	}

	// URL의 start·end (경로 패턴으로 둘만 받는다)
	private static DayCheckType toType(String type) {
		return "start".equals(type) ? DayCheckType.START : DayCheckType.END;
	}
}
