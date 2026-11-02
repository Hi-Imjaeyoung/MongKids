package com.mongkids.domain.attendance.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mongkids.domain.attendance.dto.ClassEndImageUploadUrlRequest;
import com.mongkids.domain.attendance.dto.ClassEndImageUploadUrlResponse;
import com.mongkids.domain.attendance.dto.ClassEndSaveRequest;
import com.mongkids.domain.attendance.dto.ClassSessionResponse;
import com.mongkids.domain.attendance.dto.ClassStartLocationRequest;
import com.mongkids.domain.attendance.dto.ClassStartManualRequest;
import com.mongkids.domain.attendance.dto.ClassStartNoteRequest;
import com.mongkids.domain.attendance.dto.ClassStartResultResponse;
import com.mongkids.domain.attendance.service.AttendanceService;
import com.mongkids.global.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 직원 수업 시작·종료 체크 (EMP-SCR-001)
@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

	private final AttendanceService attendanceService;

	@GetMapping("/classes/today")
	public ApiResponse<List<ClassSessionResponse>> getTodayClasses(@AuthenticationPrincipal Long memberId) {
		return ApiResponse.success(attendanceService.getTodayClasses(memberId));
	}

	// 내 수업 할당 (EMP-FR-004)
	@GetMapping("/classes")
	public ApiResponse<List<ClassSessionResponse>> getMyClasses(
		@AuthenticationPrincipal Long memberId,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
	) {
		return ApiResponse.success(attendanceService.getMyClasses(memberId, from, to));
	}

	// 반경 밖이어도 200으로 거리와 기준 반경을 준다 (EMP-FR-001)
	@PostMapping("/classes/{classSessionId}/start")
	public ApiResponse<ClassStartResultResponse> startByLocation(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long classSessionId,
		@Valid @RequestBody ClassStartLocationRequest request
	) {
		return ApiResponse.success(attendanceService.startByLocation(memberId, classSessionId, request));
	}

	@PostMapping("/classes/{classSessionId}/start/manual")
	public ApiResponse<ClassSessionResponse> startManually(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long classSessionId,
		@Valid @RequestBody ClassStartManualRequest request
	) {
		return ApiResponse.success(attendanceService.startManually(memberId, classSessionId, request));
	}

	@PatchMapping("/classes/{classSessionId}/start/note")
	public ApiResponse<ClassSessionResponse> saveStartNote(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long classSessionId,
		@Valid @RequestBody ClassStartNoteRequest request
	) {
		return ApiResponse.success(attendanceService.saveStartNote(memberId, classSessionId, request));
	}

	@PostMapping("/image-upload-urls")
	public ApiResponse<List<ClassEndImageUploadUrlResponse>> createImageUploadUrls(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody ClassEndImageUploadUrlRequest request
	) {
		return ApiResponse.success(attendanceService.createImageUploadUrls(memberId, request));
	}

	@PostMapping("/classes/{classSessionId}/end")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<ClassSessionResponse> endClass(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long classSessionId,
		@Valid @RequestBody ClassEndSaveRequest request
	) {
		return ApiResponse.success(attendanceService.endClass(memberId, classSessionId, request));
	}

	// 수업 기록 정정 (EMP-FR-014)
	@PutMapping("/classes/{classSessionId}/end")
	public ApiResponse<ClassSessionResponse> correctClassEnd(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long classSessionId,
		@Valid @RequestBody ClassEndSaveRequest request
	) {
		return ApiResponse.success(attendanceService.correctClassEnd(memberId, classSessionId, request));
	}
}
