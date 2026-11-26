package com.mongkids.domain.attendance.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mongkids.domain.attendance.dto.AdminClassSessionResponse;
import com.mongkids.domain.attendance.dto.SeriesStaffAssignRequest;
import com.mongkids.domain.attendance.dto.SeriesStaffSuggestionResponse;
import com.mongkids.domain.attendance.dto.UnassignedSeriesResponse;
import com.mongkids.domain.attendance.service.ClassSeriesService;
import com.mongkids.global.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 정기 수업(묶음) 담당 직원 추천·배정 (EMP-FR-022, EMP-SCR-007)
@RestController
@RequestMapping("/api/v1/admin/attendance/series")
@RequiredArgsConstructor
public class AdminClassSeriesController {

	private final ClassSeriesService classSeriesService;

	// 오늘 이후 직원 미정 회차가 남은 정기 수업
	@GetMapping("/unassigned")
	public ApiResponse<List<UnassignedSeriesResponse>> getUnassignedSeries(@AuthenticationPrincipal Long memberId) {
		return ApiResponse.success(classSeriesService.getUnassignedSeries(memberId));
	}

	@GetMapping("/{seriesId}/staff-suggestions")
	public ApiResponse<SeriesStaffSuggestionResponse> getStaffSuggestions(@AuthenticationPrincipal Long memberId,
		@PathVariable Long seriesId) {
		return ApiResponse.success(classSeriesService.getStaffSuggestions(memberId, seriesId));
	}

	// 오늘 이후 미정 회차를 직원 한 명에게 배정
	@PutMapping("/{seriesId}/staff")
	public ApiResponse<List<AdminClassSessionResponse>> assignStaff(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long seriesId,
		@Valid @RequestBody SeriesStaffAssignRequest request
	) {
		return ApiResponse.success(classSeriesService.assignStaff(memberId, seriesId, request));
	}
}
