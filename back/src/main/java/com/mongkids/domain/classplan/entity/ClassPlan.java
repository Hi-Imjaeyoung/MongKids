package com.mongkids.domain.classplan.entity;

import com.mongkids.global.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 수업 계획안 (SCR-027, FR-032, ADR-009).
 * fileKey는 누구나 보는 공개 PDF(연간은 계획안 자체, 월간은 관리자가 블러 처리한 PDF)이고,
 * originalFileKey는 월간 원본 PDF다. 원본은 공개 URL로 내보내지 않고 서버가 권한을 확인한 뒤 전달한다.
 */
@Entity
@Table(name = "class_plan")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassPlan extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private ClassPlanKind kind;

	@Column(nullable = false, length = 50)
	private String title;

	@Column(nullable = false, length = 100)
	private String period;

	@Column(nullable = false)
	private String fileKey;

	private String originalFileKey;

	@Builder
	private ClassPlan(ClassPlanKind kind, String title, String period, String fileKey, String originalFileKey) {
		this.kind = kind;
		this.title = title;
		this.period = period;
		this.fileKey = fileKey;
		this.originalFileKey = originalFileKey;
	}

	public void changeInfo(String title, String period) {
		this.title = title;
		this.period = period;
	}

	public void changeFile(String fileKey) {
		this.fileKey = fileKey;
	}

	public void changeOriginalFile(String originalFileKey) {
		this.originalFileKey = originalFileKey;
	}
}
