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

import com.mongkids.domain.post.dto.PostCreateResponse;
import com.mongkids.domain.post.dto.PostDetailResponse;
import com.mongkids.domain.post.dto.PostImageUploadUrlRequest;
import com.mongkids.domain.post.dto.PostImageUploadUrlResponse;
import com.mongkids.domain.post.dto.PostSaveRequest;
import com.mongkids.domain.post.dto.PostSummaryResponse;
import com.mongkids.domain.post.entity.PostCategory;
import com.mongkids.domain.post.service.PostService;
import com.mongkids.global.common.ApiResponse;
import com.mongkids.global.common.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;

	@PostMapping("/image-upload-urls")
	public ApiResponse<List<PostImageUploadUrlResponse>> createImageUploadUrls(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody PostImageUploadUrlRequest request
	) {
		return ApiResponse.success(postService.createImageUploadUrls(memberId, request));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<PostCreateResponse> createPost(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody PostSaveRequest request
	) {
		return ApiResponse.success(postService.createPost(memberId, request));
	}

	@GetMapping
	public ApiResponse<PageResponse<PostSummaryResponse>> getPosts(
		@RequestParam(required = false) PostCategory category,
		@PageableDefault(size = 12) Pageable pageable
	) {
		return ApiResponse.success(postService.getPosts(category, pageable));
	}

	@GetMapping("/{postId}")
	public ApiResponse<PostDetailResponse> getPost(@PathVariable Long postId) {
		return ApiResponse.success(postService.getPost(postId));
	}

	@PutMapping("/{postId}")
	public ApiResponse<Void> updatePost(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long postId,
		@Valid @RequestBody PostSaveRequest request
	) {
		postService.updatePost(memberId, postId, request);
		return ApiResponse.success(null);
	}

	@DeleteMapping("/{postId}")
	public ApiResponse<Void> deletePost(@AuthenticationPrincipal Long memberId, @PathVariable Long postId) {
		postService.deletePost(memberId, postId);
		return ApiResponse.success(null);
	}
}
