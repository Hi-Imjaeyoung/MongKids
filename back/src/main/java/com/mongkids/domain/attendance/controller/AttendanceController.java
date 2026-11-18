package com.mongkids.domain.attendance.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongkids.domain.attendance.dto.ClassEndImageUploadUrlRequest;
import com.mongkids.domain.attendance.dto.ClassEndImageUploadUrlResponse;
import com.mongkids.domain.attendance.dto.ClassStartLocationRequest;
import com.mongkids.domain.attendance.dto.ClassStartManualRequest;
import com.mongkids.domain.attendance.dto.DayCheckResultResponse;
import com.mongkids.domain.attendance.dto.DayEndCorrectRequest;
import com.mongkids.domain.attendance.dto.DayEndSaveRequest;
import com.mongkids.domain.attendance.dto.WorkDayResponse;
import com.mongkids.domain.attendance.service.AttendanceService;
import com.mongkids.domain.attendance.service.WorkDayService;
import com.mongkids.global.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 직원 하루 수업 시작·종료 체크 (EMP-SCR-001, EMP-FR-020)
@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

	private final AttendanceService attendanceService;
	private final WorkDayService workDayService;

	@GetMapping("/days/today")
	public ApiResponse<WorkDayResponse> getToday(@AuthenticationPrincipal Long memberId) {
		return ApiResponse.success(workDayService.getToday(memberId));
	}

	// 날짜별 내 하루 기록 (EMP-FR-015)
	@GetMapping("/days")
	public ApiResponse<List<WorkDayResponse>> getMyDays(
		@AuthenticationPrincipal Long memberId,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
	) {
		return ApiResponse.success(workDayService.getMyDays(memberId, from, to));
	}

	// 반경 밖이어도 200으로 거리와 기준 반경을 준다
	@PostMapping("/days/{date}/start")
	public ApiResponse<DayCheckResultResponse> startByLocation(
		@AuthenticationPrincipal Long memberId,
		@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
		@Valid @RequestBody ClassStartLocationRequest request
	) {
		return ApiResponse.success(workDayService.startByLocation(memberId, date, request));
	}

	@PostMapping("/days/{date}/start/manual")
	public ApiResponse<WorkDayResponse> startManually(
		@AuthenticationPrincipal Long memberId,
		@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
		@Valid @RequestBody ClassStartManualRequest request
	) {
		return ApiResponse.success(workDayService.startManually(memberId, date, request));
	}

	// 하루 종료 화면에 들어가기 전 위치 확인. 저장하지 않는다
	@PostMapping("/days/{date}/end/location-checks")
	public ApiResponse<DayCheckResultResponse> checkEndLocation(
		@AuthenticationPrincipal Long memberId,
		@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
		@Valid @RequestBody ClassStartLocationRequest request
	) {
		return ApiResponse.success(workDayService.checkEndLocation(memberId, date, request));
	}

	// 반경 밖이면 200으로 거리와 기준 반경을 주고 저장하지 않는다
	@PostMapping("/days/{date}/end")
	public ApiResponse<DayCheckResultResponse> endByLocation(
		@AuthenticationPrincipal Long memberId,
		@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
		@Valid @RequestBody DayEndSaveRequest request
	) {
		return ApiResponse.success(workDayService.endByLocation(memberId, date, request));
	}

	@PostMapping("/days/{date}/end/manual")
	public ApiResponse<WorkDayResponse> endManually(
		@AuthenticationPrincipal Long memberId,
		@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
		@Valid @RequestBody DayEndSaveRequest request
	) {
		return ApiResponse.success(workDayService.endManually(memberId, date, request));
	}

	// 하루 사진·기관별 특이사항 정정 (EMP-FR-014 규칙 유지)
	@PutMapping("/days/{date}/end")
	public ApiResponse<WorkDayResponse> correctEnd(
		@AuthenticationPrincipal Long memberId,
		@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
		@Valid @RequestBody DayEndCorrectRequest request
	) {
		return ApiResponse.success(workDayService.correctEnd(memberId, date, request));
	}

	@PostMapping("/image-upload-urls")
	public ApiResponse<List<ClassEndImageUploadUrlResponse>> createImageUploadUrls(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody ClassEndImageUploadUrlRequest request
	) {
		return ApiResponse.success(attendanceService.createImageUploadUrls(memberId, request));
	}
}
