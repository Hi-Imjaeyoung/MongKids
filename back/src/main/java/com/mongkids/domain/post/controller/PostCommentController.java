package com.mongkids.domain.post.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mongkids.domain.post.dto.PostCommentResponse;
import com.mongkids.domain.post.dto.PostCommentSaveRequest;
import com.mongkids.domain.post.service.PostCommentService;
import com.mongkids.global.common.ApiResponse;
import com.mongkids.global.common.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
public class PostCommentController {

	private final PostCommentService commentService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<PostCommentResponse> createComment(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long postId,
		@Valid @RequestBody PostCommentSaveRequest request
	) {
		return ApiResponse.success(commentService.createComment(memberId, postId, request));
	}

	@GetMapping
	public ApiResponse<PageResponse<PostCommentResponse>> getComments(
		@PathVariable Long postId,
		@PageableDefault(size = 20) Pageable pageable
	) {
		return ApiResponse.success(commentService.getComments(postId, pageable));
	}

	@DeleteMapping("/{commentId}")
	public ApiResponse<Void> deleteComment(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long postId,
		@PathVariable Long commentId
	) {
		commentService.deleteComment(memberId, postId, commentId);
		return ApiResponse.success(null);
	}
}
