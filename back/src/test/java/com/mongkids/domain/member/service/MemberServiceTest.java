package com.mongkids.domain.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.mongkids.domain.member.dto.LoginMemberResponse;
import com.mongkids.domain.member.dto.SocialLoginRequest;
import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.entity.MemberRole;
import com.mongkids.domain.member.entity.Provider;
import com.mongkids.domain.member.repository.MemberRepository;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

	private static final SocialLoginRequest REQUEST =
		new SocialLoginRequest(Provider.KAKAO, "12345", "몽키", "monkey@kakao.com");

	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private MemberService memberService;

	@Test
	@DisplayName("처음 로그인한 소셜 계정은 MEMBER 역할로 가입된다")
	void loginOrRegister_newMember_registers() {
		// given
		given(memberRepository.findByProviderAndProviderId(Provider.KAKAO, "12345")).willReturn(Optional.empty());
		given(memberRepository.save(any(Member.class))).willAnswer(invocation -> withId(invocation.getArgument(0), 1L));

		// when
		LoginMemberResponse response = memberService.loginOrRegister(REQUEST);

		// then
		assertThat(response).isEqualTo(new LoginMemberResponse(1L, MemberRole.MEMBER));
		then(memberRepository).should().save(any(Member.class));
	}

	@Test
	@DisplayName("이미 가입한 소셜 계정은 새로 저장하지 않고 기존 회원으로 로그인한다")
	void loginOrRegister_existingMember_returnsMember() {
		// given
		Member member = withId(REQUEST.toEntity(), 7L);
		member.grantStaff();
		given(memberRepository.findByProviderAndProviderId(Provider.KAKAO, "12345")).willReturn(Optional.of(member));

		// when
		LoginMemberResponse response = memberService.loginOrRegister(REQUEST);

		// then
		assertThat(response).isEqualTo(new LoginMemberResponse(7L, MemberRole.STAFF));
		then(memberRepository).should(never()).save(any());
	}

	@Test
	@DisplayName("탈퇴한 회원이 로그인하면 MEMBER_WITHDRAWN 예외가 발생한다")
	void loginOrRegister_withdrawn_throwsException() {
		// given
		Member member = withId(REQUEST.toEntity(), 7L);
		member.withdraw();
		given(memberRepository.findByProviderAndProviderId(Provider.KAKAO, "12345")).willReturn(Optional.of(member));

		// when & then
		assertThatThrownBy(() -> memberService.loginOrRegister(REQUEST))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.MEMBER_WITHDRAWN);
	}

	@Test
	@DisplayName("존재하지 않는 회원을 조회하면 MEMBER_NOT_FOUND 예외가 발생한다")
	void getMember_notFound_throwsException() {
		// given
		given(memberRepository.findById(1L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> memberService.getMember(1L))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
	}

	private static Member withId(Member member, Long id) {
		ReflectionTestUtils.setField(member, "id", id);
		return member;
	}
}
