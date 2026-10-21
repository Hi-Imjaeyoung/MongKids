package com.mongkids.domain.review.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mongkids.domain.review.dto.ReviewImageUploadUrlRequest;
import com.mongkids.domain.review.dto.ReviewImageUploadUrlResponse;
import com.mongkids.domain.review.dto.ReviewResponse;
import com.mongkids.domain.review.dto.ReviewSaveRequest;
import com.mongkids.domain.review.entity.ReviewType;
import com.mongkids.domain.review.service.ReviewService;
import com.mongkids.global.common.ApiResponse;
import com.mongkids.global.common.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

	private final ReviewService reviewService;

	@PostMapping("/image-upload-urls")
	public ApiResponse<List<ReviewImageUploadUrlResponse>> createImageUploadUrls(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody ReviewImageUploadUrlRequest request
	) {
		return ApiResponse.success(reviewService.createImageUploadUrls(memberId, request));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<ReviewResponse> createReview(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody ReviewSaveRequest request
	) {
		return ApiResponse.success(reviewService.createReview(memberId, request));
	}

	// 비로그인이면 memberId가 null이고 최신 4개만 받는다 (FR-012)
	@GetMapping
	public ApiResponse<PageResponse<ReviewResponse>> getReviews(
		@AuthenticationPrincipal Long memberId,
		@RequestParam(required = false) ReviewType type,
		@PageableDefault(size = 10) Pageable pageable
	) {
		return ApiResponse.success(reviewService.getReviews(memberId, type, pageable));
	}

	@PutMapping("/{reviewId}")
	public ApiResponse<ReviewResponse> updateReview(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long reviewId,
		@Valid @RequestBody ReviewSaveRequest request
	) {
		return ApiResponse.success(reviewService.updateReview(memberId, reviewId, request));
	}

	@DeleteMapping("/{reviewId}")
	public ApiResponse<Void> deleteReview(@AuthenticationPrincipal Long memberId, @PathVariable Long reviewId) {
		reviewService.deleteReview(memberId, reviewId);
		return ApiResponse.success(null);
	}
}
