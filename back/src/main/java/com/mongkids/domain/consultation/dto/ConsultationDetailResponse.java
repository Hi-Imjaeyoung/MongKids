package com.mongkids.domain.consultation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.mongkids.domain.consultation.entity.Consultation;
import com.mongkids.domain.consultation.entity.ConsultationStatus;
import com.mongkids.domain.consultation.entity.ConsultationType;
import com.mongkids.domain.consultation.entity.Gender;
import com.mongkids.domain.consultation.entity.InstitutionConsultation;
import com.mongkids.domain.consultation.entity.PersonalConsultation;

/**
 * 관리자 상담 신청 상세 (FR-004). 유형에 맞는 쪽({@code institution} 또는 {@code personal})만 채우고 나머지는 null.
 */
public record ConsultationDetailResponse(
	Long id,
	ConsultationType type,
	String phone,
	ConsultationStatus status,
	LocalDateTime privacyAgreedAt,
	LocalDateTime completedAt,
	LocalDateTime createdAt,
	Institution institution,
	Personal personal
) {

	public record Institution(
		String institutionName,
		String preferredSchedule,
		String childAgeRange,
		int childCount,
		String note
	) {
	}

	public record Personal(
		String guardianName,
		String childName,
		Gender childGender,
		LocalDate childBirthDate,
		Integer childAgeMonths,
		String address,
		boolean attendsDaycare,
		String temperament,
		String favoritePlay,
		String favoriteCharacter,
		String healthNote,
		LocalDateTime healthAgreedAt,
		String motivation
	) {
	}

	public static ConsultationDetailResponse from(Consultation consultation) {
		Institution institution = null;
		Personal personal = null;
		if (consultation instanceof InstitutionConsultation c) {
			institution = new Institution(c.getInstitutionName(), c.getPreferredSchedule(), c.getChildAgeRange(),
				c.getChildCount(), c.getNote());
		} else if (consultation instanceof PersonalConsultation c) {
			personal = new Personal(c.getGuardianName(), c.getChildName(), c.getChildGender(), c.getChildBirthDate(),
				c.getChildAgeMonths(), c.getAddress(), c.isAttendsDaycare(), c.getTemperament(), c.getFavoritePlay(),
				c.getFavoriteCharacter(), c.getHealthNote(), c.getHealthAgreedAt(), c.getMotivation());
		}
		return new ConsultationDetailResponse(
			consultation.getId(),
			consultation.getType(),
			consultation.getPhone(),
			consultation.getStatus(),
			consultation.getPrivacyAgreedAt(),
			consultation.getCompletedAt(),
			consultation.getCreatedAt(),
			institution,
			personal
		);
	}
}
