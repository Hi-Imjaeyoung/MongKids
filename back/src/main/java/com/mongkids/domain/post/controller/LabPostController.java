package com.mongkids.domain.post.controller;

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

import com.mongkids.domain.post.dto.LabPostDetailResponse;
import com.mongkids.domain.post.dto.LabPostSaveRequest;
import com.mongkids.domain.post.dto.LabPostSummaryResponse;
import com.mongkids.domain.post.dto.PostCreateResponse;
import com.mongkids.domain.post.dto.PostImageUploadUrlRequest;
import com.mongkids.domain.post.dto.PostImageUploadUrlResponse;
import com.mongkids.domain.post.entity.LabCategory;
import com.mongkids.domain.post.service.LabPostService;
import com.mongkids.global.common.ApiResponse;
import com.mongkids.global.common.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 몽키즈 연구소 글. 사진 업로드 URL은 게시물과 요청·응답 형식이 같고, 키만 {@code lab/} 아래로 발급한다.
 */
@RestController
@RequestMapping("/api/v1/lab-posts")
@RequiredArgsConstructor
public class LabPostController {

	private final LabPostService labPostService;

	@PostMapping("/image-upload-urls")
	public ApiResponse<List<PostImageUploadUrlResponse>> createImageUploadUrls(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody PostImageUploadUrlRequest request
	) {
		return ApiResponse.success(labPostService.createImageUploadUrls(memberId, request));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<PostCreateResponse> createLabPost(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody LabPostSaveRequest request
	) {
		return ApiResponse.success(labPostService.createLabPost(memberId, request));
	}

	@GetMapping
	public ApiResponse<PageResponse<LabPostSummaryResponse>> getLabPosts(
		@RequestParam(required = false) LabCategory category,
		@RequestParam(required = false) String keyword,
		@PageableDefault(size = 12) Pageable pageable
	) {
		return ApiResponse.success(labPostService.getLabPosts(category, keyword, pageable));
	}

	@GetMapping("/{labPostId}")
	public ApiResponse<LabPostDetailResponse> getLabPost(@PathVariable Long labPostId) {
		return ApiResponse.success(labPostService.getLabPost(labPostId));
	}

	@PutMapping("/{labPostId}")
	public ApiResponse<Void> updateLabPost(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long labPostId,
		@Valid @RequestBody LabPostSaveRequest request
	) {
		labPostService.updateLabPost(memberId, labPostId, request);
		return ApiResponse.success(null);
	}

	@DeleteMapping("/{labPostId}")
	public ApiResponse<Void> deleteLabPost(@AuthenticationPrincipal Long memberId, @PathVariable Long labPostId) {
		labPostService.deleteLabPost(memberId, labPostId);
		return ApiResponse.success(null);
	}
}
