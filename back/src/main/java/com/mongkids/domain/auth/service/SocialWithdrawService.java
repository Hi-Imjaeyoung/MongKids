package com.mongkids.domain.auth.service;

import org.springframework.stereotype.Service;

import com.mongkids.domain.auth.dto.OAuth2WithdrawPrincipal;
import com.mongkids.domain.member.service.MemberService;

import lombok.RequiredArgsConstructor;

/**
 * 회원 탈퇴 (FR-028). 본인 확인 → 소셜 연결 끊기 → 개인정보 파기 순서로 진행한다.
 * 연결 끊기가 실패하면 예외가 나서 파기하지 않는다 (사용자 결정 2026-10-30).
 */
@Service
@RequiredArgsConstructor
public class SocialWithdrawService {

	private final MemberService memberService;
	private final SocialUnlinkClient socialUnlinkClient;

	public void withdraw(Long memberId, OAuth2WithdrawPrincipal principal) {
		memberService.validateWithdrawable(memberId, principal.provider(), principal.providerId());
		socialUnlinkClient.unlink(principal.provider(), principal.accessToken());
		memberService.withdraw(memberId, principal.provider(), principal.providerId());
	}
}
