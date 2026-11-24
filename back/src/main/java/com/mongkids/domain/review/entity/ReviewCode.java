package com.mongkids.domain.review.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.mongkids.global.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 후기 작성 코드 (FR-029, ADR-005). 코드를 받은 회원만 후기를 쓸 수 있게 관리자가 발급한다.
 * 코드 확인·사용 횟수 차감(FR-030)은 후기 작성에서 한다. 새로 모으는 개인정보는 없고,
 * 메모는 묶음 코드를 어디에 나눠 줬는지 적는 칸이다.
 */
@Entity
@Table(
	name = "review_code",
	indexes = @Index(name = "uk_review_code_code", columnList = "code", unique = true)
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewCode extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 20)
	private String code;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "varchar(10)")
	private ReviewCodeKind kind;

	@Column(nullable = false)
	private int maxUses;

	@Column(nullable = false)
	private int usedCount;

	// 이날까지 쓸 수 있다
	@Column(nullable = false)
	private LocalDate expiresOn;

	@Column(length = 50)
	private String memo;

	private LocalDateTime revokedAt;

	@Builder
	private ReviewCode(String code, ReviewCodeKind kind, int maxUses, LocalDate expiresOn, String memo) {
		this.code = code;
		this.kind = kind;
		this.maxUses = maxUses;
		this.expiresOn = expiresOn;
		this.memo = memo;
	}

	public ReviewCodeStatus status(LocalDate today) {
		if (revokedAt != null) {
			return ReviewCodeStatus.REVOKED;
		}
		if (usedCount >= maxUses) {
			return ReviewCodeStatus.USED_UP;
		}
		if (expiresOn.isBefore(today)) {
			return ReviewCodeStatus.EXPIRED;
		}
		return ReviewCodeStatus.ACTIVE;
	}

	public void revoke(LocalDateTime now) {
		this.revokedAt = now;
	}
}
