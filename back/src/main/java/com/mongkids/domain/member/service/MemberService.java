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
import com.mongkids.domain.member.dto.SocialLoginRequest;
import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.entity.MemberRole;
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
		return MemberResponse.from(getActiveMember(memberId));
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
