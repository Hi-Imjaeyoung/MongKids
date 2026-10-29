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
import com.mongkids.domain.attendance.dto.ClassStartReasonRequest;
import com.mongkids.domain.attendance.service.AttendanceService;
import com.mongkids.global.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 근태 관리자 기능 (EMP-FR-003, EMP-FR-011). 화면(EMP-SCR-006)은 #63에서 만든다
@RestController
@RequestMapping("/api/v1/admin/attendance")
@RequiredArgsConstructor
public class AdminAttendanceController {

	private final AttendanceService attendanceService;

	@GetMapping("/pending-starts")
	public ApiResponse<List<AdminClassSessionResponse>> getPendingStarts(@AuthenticationPrincipal Long memberId) {
		return ApiResponse.success(attendanceService.getPendingStarts(memberId));
	}

	@GetMapping("/classes")
	public ApiResponse<List<AdminClassSessionResponse>> getClassRecords(
		@AuthenticationPrincipal Long memberId,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
		@RequestParam(required = false) Long staffId
	) {
		return ApiResponse.success(attendanceService.getClassRecords(memberId, from, to, staffId));
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

	@PostMapping("/classes/{classSessionId}/start")
	public ApiResponse<AdminClassSessionResponse> registerStart(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long classSessionId,
		@Valid @RequestBody ClassStartReasonRequest request
	) {
		return ApiResponse.success(attendanceService.registerStartByAdmin(memberId, classSessionId, request));
	}
}
