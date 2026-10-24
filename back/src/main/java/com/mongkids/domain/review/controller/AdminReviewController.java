package com.mongkids.domain.review.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongkids.domain.review.dto.AdminReviewResponse;
import com.mongkids.domain.review.dto.ReviewHiddenRequest;
import com.mongkids.domain.review.entity.ReviewType;
import com.mongkids.domain.review.service.ReviewService;
import com.mongkids.global.common.ApiResponse;
import com.mongkids.global.common.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 관리자 화면(SCR-014)의 후기 관리 (FR-021). 답글(FR-017)은 SCR-012 카드에서 /api/v1/reviews/{id}/reply로 단다
@RestController
@RequestMapping("/api/v1/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

	private final ReviewService reviewService;

	@GetMapping
	public ApiResponse<PageResponse<AdminReviewResponse>> getReviews(
		@AuthenticationPrincipal Long memberId,
		@RequestParam(required = false) ReviewType type,
		@RequestParam(defaultValue = "false") boolean hiddenOnly,
		@PageableDefault(size = 20) Pageable pageable
	) {
		return ApiResponse.success(reviewService.getAdminReviews(memberId, type, hiddenOnly, pageable));
	}

	@PutMapping("/{reviewId}/hidden")
	public ApiResponse<AdminReviewResponse> changeHidden(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long reviewId,
		@Valid @RequestBody ReviewHiddenRequest request
	) {
		return ApiResponse.success(reviewService.changeHidden(memberId, reviewId, request));
	}
}
