package com.mongkids.domain.member.controller;

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

import com.mongkids.domain.member.dto.AdminMemberResponse;
import com.mongkids.domain.member.dto.MemberRoleRequest;
import com.mongkids.domain.member.entity.MemberRole;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.global.common.ApiResponse;
import com.mongkids.global.common.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 관리자 화면(SCR-014)의 회원·직원 권한 (FR-022). 관리자인지는 MemberService가 DB의 현재 역할로 검사한다
@RestController
@RequestMapping("/api/v1/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

	private final MemberService memberService;

	@GetMapping
	public ApiResponse<PageResponse<AdminMemberResponse>> getMembers(
		@AuthenticationPrincipal Long memberId,
		@RequestParam(required = false) MemberRole role,
		@RequestParam(required = false) String keyword,
		@PageableDefault(size = 20) Pageable pageable
	) {
		return ApiResponse.success(memberService.getMembers(memberId, role, keyword, pageable));
	}

	@PutMapping("/{targetMemberId}/role")
	public ApiResponse<AdminMemberResponse> changeRole(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long targetMemberId,
		@Valid @RequestBody MemberRoleRequest request
	) {
		return ApiResponse.success(memberService.changeRole(memberId, targetMemberId, request));
	}
}
