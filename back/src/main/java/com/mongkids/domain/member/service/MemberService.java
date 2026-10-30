package com.mongkids.domain.member.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.mongkids.domain.member.dto.AdminMemberResponse;
import com.mongkids.domain.member.dto.LoginMemberResponse;
import com.mongkids.domain.member.dto.MemberResponse;
import com.mongkids.domain.member.dto.MemberRoleRequest;
import com.mongkids.domain.member.dto.SnsAgreementRequest;
import com.mongkids.domain.member.dto.SocialLoginRequest;
import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.entity.MemberRole;
import com.mongkids.domain.member.entity.Provider;
import com.mongkids.domain.member.repository.MemberRepository;
import com.mongkids.global.common.PageResponse;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

	static final int MAX_PAGE_SIZE = 100;
	static final Sort LATEST = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

	private final MemberRepository memberRepository;

	/**
	 * 소셜 계정으로 로그인한다. 처음 로그인하면 회원으로 가입시킨다 (FR-018).
	 * 소셜 서비스에서 받은 이메일·휴대폰 번호는 가입할 때 저장하고, 이후 로그인마다 갱신한다 (FR-019).
	 */
	@Transactional
	public LoginMemberResponse loginOrRegister(SocialLoginRequest request) {
		Member member = memberRepository.findByProviderAndProviderId(request.provider(), request.providerId())
			.orElseGet(() -> memberRepository.save(request.toEntity()));
		if (member.isWithdrawn()) {
			throw new BusinessException(ErrorCode.MEMBER_WITHDRAWN);
		}
		member.updateSocialContact(request.email(), request.phone());
		return LoginMemberResponse.from(member);
	}

	public MemberResponse getMember(Long memberId) {
		return MemberResponse.from(getActiveMember(memberId));
	}

	/**
	 * 본인의 SNS 수신 동의를 바꾼다 (FR-019). 동의하면 시각을 저장하고 철회하면 지운다.
	 */
	@Transactional
	public MemberResponse changeSnsAgreement(Long memberId, SnsAgreementRequest request) {
		Member member = getActiveMember(memberId);
		member.changeSnsAgreement(request.agreed());
		return MemberResponse.from(member);
	}

	/**
	 * 회원을 탈퇴시키고 개인정보를 지운다 (FR-028). 관리자·직원은 탈퇴할 수 없고,
	 * 본인 확인으로 다시 로그인한 소셜 계정이 가입한 계정과 같아야 한다.
	 * 소셜 연결을 끊기 전에 DB 제약 위반 등을 드러내도록 바로 flush한다 (SocialWithdrawService).
	 */
	@Transactional
	public void withdraw(Long memberId, Provider provider, String providerId) {
		Member member = getActiveMember(memberId);
		checkWithdrawable(member, provider, providerId);
		member.withdraw();
		memberRepository.flush();
	}

	private void checkWithdrawable(Member member, Provider provider, String providerId) {
		if (!member.getRole().canWithdraw()) {
			throw new BusinessException(ErrorCode.MEMBER_WITHDRAW_NOT_ALLOWED);
		}
		if (!member.isSocialAccount(provider, providerId)) {
			throw new BusinessException(ErrorCode.WITHDRAW_ACCOUNT_MISMATCH);
		}
	}

	/**
	 * 관리자 화면(SCR-014) API의 권한 검사. 토큰이 아니라 DB의 현재 역할로 확인한다.
	 */
	public void validateAdmin(Long memberId) {
		if (!getActiveMember(memberId).getRole().isAdmin()) {
			throw new BusinessException(ErrorCode.FORBIDDEN);
		}
	}

	/**
	 * 회원 목록 (FR-022). 탈퇴 회원은 빼고 최근 가입 순이다.
	 * 검색어는 이름 부분 일치, 숫자가 들어 있으면 전화번호(하이픈 무시) 부분 일치로도 찾는다.
	 */
	public PageResponse<AdminMemberResponse> getMembers(Long adminId, MemberRole role, String keyword,
		Pageable pageable) {
		validateAdmin(adminId);
		String name = StringUtils.hasText(keyword) ? keyword.strip() : null;
		String phoneDigits = name == null ? "" : name.replaceAll("\\D", "");
		Pageable latest = PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), MAX_PAGE_SIZE),
			LATEST);
		return PageResponse.from(
			memberRepository.search(role, name, phoneDigits.isEmpty() ? null : phoneDigits, latest),
			AdminMemberResponse::from);
	}

	/**
	 * 직원 권한을 주거나 회수한다 (FR-022). 관리자의 역할은 바꿀 수 없고 관리자 권한을 줄 수도 없다.
	 * 회수해도 작성한 게시물은 남고, 새 글 작성과 자기 글 수정·삭제만 막힌다 (FR-009).
	 */
	@Transactional
	public AdminMemberResponse changeRole(Long adminId, Long memberId, MemberRoleRequest request) {
		validateAdmin(adminId);
		Member member = getActiveMember(memberId);
		if (member.getRole().isAdmin() || request.role().isAdmin()) {
			throw new BusinessException(ErrorCode.ADMIN_ROLE_UNCHANGEABLE);
		}
		if (request.role() == MemberRole.STAFF) {
			member.grantStaff();
		} else {
			member.revokeStaff();
		}
		return AdminMemberResponse.from(member);
	}

	/**
	 * 탈퇴하지 않은 회원 Entity. 다른 도메인이 연관관계를 맺을 때 쓴다.
	 */
	public Member getActiveMember(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
		if (member.isWithdrawn()) {
			throw new BusinessException(ErrorCode.MEMBER_WITHDRAWN);
		}
		return member;
	}
}
