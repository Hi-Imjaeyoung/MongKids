package com.mongkids.domain.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mongkids.domain.auth.dto.OAuth2WithdrawPrincipal;
import com.mongkids.domain.member.service.MemberService;

import lombok.RequiredArgsConstructor;

/**
 * 회원 탈퇴 (FR-028). 본인 확인 → 개인정보 파기 → 소셜 연결 끊기를 한 트랜잭션으로 묶는다.
 * 파기를 DB에 먼저 반영(flush)해 두고 연결을 끊으므로, 파기가 실패하면 연결을 끊지 않고,
 * 연결 끊기가 실패하면 파기를 되돌린다 (사용자 결정 2026-10-30: 연결 끊기 실패 시 탈퇴 중단).
 */
@Service
@RequiredArgsConstructor
public class SocialWithdrawService {

	private final MemberService memberService;
	private final SocialUnlinkClient socialUnlinkClient;

	@Transactional
	public void withdraw(Long memberId, OAuth2WithdrawPrincipal principal) {
		memberService.withdraw(memberId, principal.provider(), principal.providerId());
		socialUnlinkClient.unlink(principal.provider(), principal.accessToken());
	}
}
