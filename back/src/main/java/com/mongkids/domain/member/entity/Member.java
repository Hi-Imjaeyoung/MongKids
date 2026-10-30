package com.mongkids.domain.member.entity;

import java.time.LocalDateTime;

import com.mongkids.global.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "member",
	uniqueConstraints = @UniqueConstraint(name = "uk_member_provider", columnNames = {"provider", "provider_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

	public static final String WITHDRAWN_NAME = "탈퇴 회원";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "varchar(10)")
	private Provider provider;

	// 탈퇴하면 지운다 (FR-028). 같은 소셜 계정으로 다시 가입할 수 있도록 NULL을 허용한다
	@Column(length = 100)
	private String providerId;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(length = 255)
	private String email;

	@Column(length = 20)
	private String phone;

	@Column(nullable = false)
	private boolean snsAgreed;

	private LocalDateTime snsAgreedAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "varchar(10)")
	private MemberRole role;

	private LocalDateTime deletedAt;

	@Builder
	private Member(Provider provider, String providerId, String name, String email) {
		this.provider = provider;
		this.providerId = providerId;
		this.name = name;
		this.email = email;
		this.role = MemberRole.MEMBER;
	}

	public void registerSignUpInfo(String phone, boolean snsAgreed) {
		this.phone = phone;
		changeSnsAgreement(snsAgreed);
	}

	public void changeSnsAgreement(boolean snsAgreed) {
		this.snsAgreed = snsAgreed;
		this.snsAgreedAt = snsAgreed ? LocalDateTime.now() : null;
	}

	public void grantStaff() {
		this.role = MemberRole.STAFF;
	}

	public void revokeStaff() {
		this.role = MemberRole.MEMBER;
	}

	/**
	 * 탈퇴 표시와 함께 개인정보를 바로 지운다 (FR-028). 작성한 글은 남고 작성자는 "탈퇴 회원"으로 보인다.
	 */
	public void withdraw() {
		this.deletedAt = LocalDateTime.now();
		this.name = WITHDRAWN_NAME;
		this.providerId = null;
		this.email = null;
		this.phone = null;
		changeSnsAgreement(false);
	}

	public boolean isSocialAccount(Provider provider, String providerId) {
		return this.provider == provider && providerId != null && providerId.equals(this.providerId);
	}

	public boolean isWithdrawn() {
		return deletedAt != null;
	}
}
