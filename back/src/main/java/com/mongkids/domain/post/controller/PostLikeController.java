package com.mongkids.domain.post.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mongkids.domain.post.dto.LikeResponse;
import com.mongkids.domain.post.service.PostLikeService;
import com.mongkids.global.common.ApiResponse;

import lombok.RequiredArgsConstructor;

/**
 * 공감 (FR-011). 조회는 비로그인도 되고(liked=false), 공감·취소는 로그인이 필요하다.
 */
@RestController
@RequiredArgsConstructor
public class PostLikeController {

	private final PostLikeService likeService;

	@GetMapping("/api/v1/posts/{postId}/likes")
	public ApiResponse<LikeResponse> getPostLike(@AuthenticationPrincipal Long memberId, @PathVariable Long postId) {
		return ApiResponse.success(likeService.getPostLike(memberId, postId));
	}

	@PostMapping("/api/v1/posts/{postId}/likes")
	public ApiResponse<LikeResponse> likePost(@AuthenticationPrincipal Long memberId, @PathVariable Long postId) {
		return ApiResponse.success(likeService.likePost(memberId, postId));
	}

	@DeleteMapping("/api/v1/posts/{postId}/likes")
	public ApiResponse<LikeResponse> unlikePost(@AuthenticationPrincipal Long memberId, @PathVariable Long postId) {
		return ApiResponse.success(likeService.unlikePost(memberId, postId));
	}

	@GetMapping("/api/v1/lab-posts/{labPostId}/likes")
	public ApiResponse<LikeResponse> getLabPostLike(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long labPostId
	) {
		return ApiResponse.success(likeService.getLabPostLike(memberId, labPostId));
	}

	@PostMapping("/api/v1/lab-posts/{labPostId}/likes")
	public ApiResponse<LikeResponse> likeLabPost(@AuthenticationPrincipal Long memberId, @PathVariable Long labPostId) {
		return ApiResponse.success(likeService.likeLabPost(memberId, labPostId));
	}

	@DeleteMapping("/api/v1/lab-posts/{labPostId}/likes")
	public ApiResponse<LikeResponse> unlikeLabPost(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long labPostId
	) {
		return ApiResponse.success(likeService.unlikeLabPost(memberId, labPostId));
	}
}
