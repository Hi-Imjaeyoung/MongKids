package com.mongkids.domain.consultation.entity;

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
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 기관·개인 폼의 항목이 크게 달라 JOINED로 나누고, 관리자 목록 조회는 이 테이블 하나로 처리한다.
@Entity
@Table(
	name = "consultation",
	indexes = @Index(name = "idx_consultation_status_created_at", columnList = "status, created_at")
)
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Consultation extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 판별 컬럼(@DiscriminatorColumn)은 Hibernate가 CHECK 제약을 붙여 값 추가가 번거로워 일반 컬럼으로 둔다.
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false, columnDefinition = "varchar(20)")
	private ConsultationType type;

	@Column(nullable = false, length = 20)
	private String phone;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "varchar(20)")
	private ConsultationStatus status;

	@Column(nullable = false)
	private LocalDateTime privacyAgreedAt;

	protected Consultation(ConsultationType type, String phone, LocalDateTime privacyAgreedAt) {
		this.type = type;
		this.phone = phone;
		this.privacyAgreedAt = privacyAgreedAt;
		this.status = ConsultationStatus.RECEIVED;
	}

	public void markContacted() {
		this.status = ConsultationStatus.CONTACTED;
	}
}
