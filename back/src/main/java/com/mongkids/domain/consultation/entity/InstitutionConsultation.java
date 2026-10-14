package com.mongkids.domain.consultation.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "institution_consultation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InstitutionConsultation extends Consultation {

	@Column(nullable = false, length = 100)
	private String institutionName;

	@Column(nullable = false, length = 255)
	private String preferredSchedule;

	@Column(nullable = false, length = 50)
	private String childAgeRange;

	@Column(nullable = false)
	private int childCount;

	@Column(columnDefinition = "TEXT")
	private String note;

	@Builder
	private InstitutionConsultation(String phone, LocalDateTime privacyAgreedAt, String institutionName,
		String preferredSchedule, String childAgeRange, int childCount, String note) {
		super(ConsultationType.INSTITUTION, phone, privacyAgreedAt);
		this.institutionName = institutionName;
		this.preferredSchedule = preferredSchedule;
		this.childAgeRange = childAgeRange;
		this.childCount = childCount;
		this.note = note;
	}
}
