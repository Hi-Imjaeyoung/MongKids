package com.mongkids.domain.content.entity;

import com.mongkids.global.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ABOUT 몽키즈(SCR-002) 월별 커리큘럼 예시 한 줄 (FR-034, ADR-008).
 * 관리자가 저장한 값만 둔다. 행이 없으면 화면은 코드(aboutContent.js)의 기본 문구를 쓴다.
 * 한 줄은 월·활동 주제·목표 세 칸이다. 줄 수는 3줄 고정이고 sortOrder(1~3)가 표시 순서다. month는 H2 예약어라 컬럼명을 month_label로 둔다.
 */
@Entity
@Table(name = "about_curriculum_month",
	uniqueConstraints = @UniqueConstraint(name = "uk_about_curriculum_month_sort_order", columnNames = "sort_order"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AboutCurriculumMonth extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private int sortOrder;

	@Column(name = "month_label", nullable = false, length = 10)
	private String month;

	@Column(nullable = false, length = 40)
	private String topic;

	@Column(nullable = false, length = 40)
	private String goal;

	@Builder
	private AboutCurriculumMonth(int sortOrder, String month, String topic, String goal) {
		this.sortOrder = sortOrder;
		this.month = month;
		this.topic = topic;
		this.goal = goal;
	}

	public void change(String month, String topic, String goal) {
		this.month = month;
		this.topic = topic;
		this.goal = goal;
	}
}
