package com.mongkids.domain.content.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mongkids.domain.content.dto.AboutCurriculumMonthResponse;
import com.mongkids.domain.content.dto.AboutCurriculumSaveRequest;
import com.mongkids.domain.content.service.AboutCurriculumService;
import com.mongkids.global.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// ABOUT 몽키즈(SCR-002) 월별 커리큘럼 저장·기본값 복원 (FR-034). 입력은 ABOUT 화면에서 관리자가 직접 한다
@RestController
@RequestMapping("/api/v1/admin/about-curriculum-months")
@RequiredArgsConstructor
public class AdminAboutCurriculumController {

	private final AboutCurriculumService aboutCurriculumService;

	@PutMapping
	public ApiResponse<List<AboutCurriculumMonthResponse>> saveMonths(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody AboutCurriculumSaveRequest request
	) {
		return ApiResponse.success(aboutCurriculumService.saveMonths(memberId, request));
	}

	@DeleteMapping
	public ApiResponse<Void> resetMonths(@AuthenticationPrincipal Long memberId) {
		aboutCurriculumService.resetMonths(memberId);
		return ApiResponse.success(null);
	}
}
