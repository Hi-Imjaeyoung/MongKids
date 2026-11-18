package com.mongkids.domain.attendance.entity;

import com.mongkids.global.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 하루 수업 사진 (EMP-FR-020). 기관 구분 없이 하루 묶음이다.
 */
@Entity
@Table(name = "day_end_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DayEndImage extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "day_end_id", nullable = false)
	private DayEnd dayEnd;

	@Column(nullable = false, length = 255)
	private String imageKey;

	@Column(nullable = false)
	private int sortOrder;

	DayEndImage(DayEnd dayEnd, String imageKey, int sortOrder) {
		this.dayEnd = dayEnd;
		this.imageKey = imageKey;
		this.sortOrder = sortOrder;
	}
}
