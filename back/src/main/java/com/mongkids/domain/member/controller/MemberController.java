package com.mongkids.domain.member.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mongkids.domain.member.dto.MemberResponse;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.global.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;

	@GetMapping("/me")
	public ApiResponse<MemberResponse> getMyInfo(@AuthenticationPrincipal Long memberId) {
		return ApiResponse.success(memberService.getMember(memberId));
	}
}
