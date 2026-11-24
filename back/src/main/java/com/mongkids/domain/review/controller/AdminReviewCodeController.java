package com.mongkids.domain.review.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mongkids.domain.review.dto.ReviewCodeCreateRequest;
import com.mongkids.domain.review.dto.ReviewCodeResponse;
import com.mongkids.domain.review.service.ReviewCodeService;
import com.mongkids.global.common.ApiResponse;
import com.mongkids.global.common.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 관리자 화면(SCR-014)의 후기 작성 코드 (FR-029). 후기 작성 때 코드 확인(FR-030)은 따로 만든다
@RestController
@RequestMapping("/api/v1/admin/review-codes")
@RequiredArgsConstructor
public class AdminReviewCodeController {

	private final ReviewCodeService reviewCodeService;

	@GetMapping
	public ApiResponse<PageResponse<ReviewCodeResponse>> getReviewCodes(
		@AuthenticationPrincipal Long memberId,
		@RequestParam(required = false) Boolean active,
		@PageableDefault(size = 20) Pageable pageable
	) {
		return ApiResponse.success(reviewCodeService.getReviewCodes(memberId, active, pageable));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<ReviewCodeResponse> createReviewCode(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody ReviewCodeCreateRequest request
	) {
		return ApiResponse.success(reviewCodeService.createReviewCode(memberId, request));
	}

	@PostMapping("/{reviewCodeId}/revoke")
	public ApiResponse<ReviewCodeResponse> revokeReviewCode(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long reviewCodeId
	) {
		return ApiResponse.success(reviewCodeService.revokeReviewCode(memberId, reviewCodeId));
	}
}
