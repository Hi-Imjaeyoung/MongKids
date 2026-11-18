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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 기관별 특이사항 (EMP-FR-020). 그날 수업한 기관마다 최대 1건, 비어 있으면 저장하지 않는다.
 */
@Entity
@Table(
	name = "day_end_note",
	uniqueConstraints = @UniqueConstraint(name = "uk_day_end_note_institution",
		columnNames = {"day_end_id", "institution_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DayEndNote extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "day_end_id", nullable = false)
	private DayEnd dayEnd;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "institution_id", nullable = false)
	private Institution institution;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String note;

	DayEndNote(DayEnd dayEnd, Institution institution, String note) {
		this.dayEnd = dayEnd;
		this.institution = institution;
		this.note = note;
	}

	void changeNote(String note) {
		this.note = note;
	}
}
