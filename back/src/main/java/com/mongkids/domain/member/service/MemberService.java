package com.mongkids.domain.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mongkids.domain.member.dto.LoginMemberResponse;
import com.mongkids.domain.member.dto.MemberResponse;
import com.mongkids.domain.member.dto.SocialLoginRequest;
import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.repository.MemberRepository;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;

	/**
	 * 소셜 계정으로 로그인한다. 처음 로그인하면 회원으로 가입시킨다 (FR-018).
	 */
	@Transactional
	public LoginMemberResponse loginOrRegister(SocialLoginRequest request) {
		Member member = memberRepository.findByProviderAndProviderId(request.provider(), request.providerId())
			.orElseGet(() -> memberRepository.save(request.toEntity()));
		if (member.isWithdrawn()) {
			throw new BusinessException(ErrorCode.MEMBER_WITHDRAWN);
		}
		return LoginMemberResponse.from(member);
	}

	public MemberResponse getMember(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
		if (member.isWithdrawn()) {
			throw new BusinessException(ErrorCode.MEMBER_WITHDRAWN);
		}
		return MemberResponse.from(member);
	}
}
