package com.mongkids.domain.classplan.controller;

import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mongkids.domain.classplan.dto.ClassPlanCreateRequest;
import com.mongkids.domain.classplan.dto.ClassPlanFileUploadUrlRequest;
import com.mongkids.domain.classplan.dto.ClassPlanFileUploadUrlResponse;
import com.mongkids.domain.classplan.dto.ClassPlanResponse;
import com.mongkids.domain.classplan.dto.ClassPlanUpdateRequest;
import com.mongkids.domain.classplan.service.ClassPlanService;
import com.mongkids.global.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 수업 계획안(SCR-027) 등록·수정·삭제와 월간 원본 PDF (FR-032, ADR-009). 입력은 /plans 화면에서 관리자가 직접 한다
@RestController
@RequestMapping("/api/v1/admin/class-plans")
@RequiredArgsConstructor
public class AdminClassPlanController {

	private final ClassPlanService classPlanService;

	@PostMapping("/file-upload-urls")
	public ApiResponse<List<ClassPlanFileUploadUrlResponse>> createFileUploadUrls(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody ClassPlanFileUploadUrlRequest request
	) {
		return ApiResponse.success(classPlanService.createFileUploadUrls(memberId, request));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<ClassPlanResponse> createClassPlan(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody ClassPlanCreateRequest request
	) {
		return ApiResponse.success(classPlanService.createClassPlan(memberId, request));
	}

	@PutMapping("/{classPlanId}")
	public ApiResponse<ClassPlanResponse> updateClassPlan(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long classPlanId,
		@Valid @RequestBody ClassPlanUpdateRequest request
	) {
		return ApiResponse.success(classPlanService.updateClassPlan(memberId, classPlanId, request));
	}

	@DeleteMapping("/{classPlanId}")
	public ApiResponse<Void> deleteClassPlan(@AuthenticationPrincipal Long memberId, @PathVariable Long classPlanId) {
		classPlanService.deleteClassPlan(memberId, classPlanId);
		return ApiResponse.success(null);
	}

	// 월간 원본. 브라우저·중간 캐시에 남기지 않는다
	@GetMapping("/{classPlanId}/original-file")
	public ResponseEntity<InputStreamResource> getOriginalFile(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long classPlanId
	) {
		return ClassPlanPdf.of(classPlanService.openOriginalFile(memberId, classPlanId), CacheControl.noStore());
	}
}
