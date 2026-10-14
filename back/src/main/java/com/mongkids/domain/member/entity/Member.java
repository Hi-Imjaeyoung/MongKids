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

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "varchar(10)")
	private Provider provider;

	@Column(nullable = false, length = 100)
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

	public void withdraw() {
		this.deletedAt = LocalDateTime.now();
	}

	public boolean isWithdrawn() {
		return deletedAt != null;
	}
}
