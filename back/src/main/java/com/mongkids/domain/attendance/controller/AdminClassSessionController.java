package com.mongkids.domain.attendance.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mongkids.domain.attendance.dto.AdminClassSessionResponse;
import com.mongkids.domain.attendance.dto.ClassSessionCreateRequest;
import com.mongkids.domain.attendance.dto.ClassSessionUpdateRequest;
import com.mongkids.domain.attendance.service.ClassSessionService;
import com.mongkids.global.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 기관 수업 할당 (EMP-FR-004, EMP-SCR-006). 목록은 GET /api/v1/admin/attendance/classes
@RestController
@RequestMapping("/api/v1/admin/attendance/classes")
@RequiredArgsConstructor
public class AdminClassSessionController {

	private final ClassSessionService classSessionService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<List<AdminClassSessionResponse>> createClassSessions(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody ClassSessionCreateRequest request
	) {
		return ApiResponse.success(classSessionService.createClassSessions(memberId, request));
	}

	@PutMapping("/{classSessionId}")
	public ApiResponse<AdminClassSessionResponse> updateClassSession(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long classSessionId,
		@Valid @RequestBody ClassSessionUpdateRequest request
	) {
		return ApiResponse.success(classSessionService.updateClassSession(memberId, classSessionId, request));
	}

	@DeleteMapping("/{classSessionId}")
	public ApiResponse<Void> deleteClassSession(@AuthenticationPrincipal Long memberId,
		@PathVariable Long classSessionId) {
		classSessionService.deleteClassSession(memberId, classSessionId);
		return ApiResponse.success(null);
	}
}
