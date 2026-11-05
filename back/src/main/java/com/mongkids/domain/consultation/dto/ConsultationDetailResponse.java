package com.mongkids.domain.consultation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.mongkids.domain.consultation.entity.Consultation;
import com.mongkids.domain.consultation.entity.ConsultationStatus;
import com.mongkids.domain.consultation.entity.ConsultationType;
import com.mongkids.domain.consultation.entity.Gender;
import com.mongkids.domain.consultation.entity.InstitutionConsultation;
import com.mongkids.domain.consultation.entity.ParticipantGroup;
import com.mongkids.domain.consultation.entity.PersonalConsultation;
import com.mongkids.domain.consultation.entity.QuoteConsultation;
import com.mongkids.domain.consultation.entity.QuoteProgram;
import com.mongkids.domain.consultation.entity.VenueType;

/**
 * 관리자 상담 신청 상세 (FR-004). 유형에 맞는 쪽({@code institution}, {@code personal}, {@code quote})만 채우고
 * 나머지는 null.
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
	Personal personal,
	Quote quote
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

	// 운동회 견적 신청 (FR-031). 고르지 않은 희망 프로그램은 빈 목록
	public record Quote(
		String institutionName,
		boolean scheduleUndecided,
		LocalDate eventDate,
		LocalTime eventTime,
		VenueType venueType,
		String address,
		List<ParticipantGroup> participantGroups,
		Integer childCount,
		Integer guardianCount,
		List<QuoteProgram> programs,
		String note
	) {
	}

	public static ConsultationDetailResponse from(Consultation consultation) {
		Institution institution = null;
		Personal personal = null;
		Quote quote = null;
		if (consultation instanceof InstitutionConsultation c) {
			institution = new Institution(c.getInstitutionName(), c.getPreferredSchedule(), c.getChildAgeRange(),
				c.getChildCount(), c.getNote());
		} else if (consultation instanceof PersonalConsultation c) {
			personal = new Personal(c.getGuardianName(), c.getChildName(), c.getChildGender(), c.getChildBirthDate(),
				c.getChildAgeMonths(), c.getAddress(), c.isAttendsDaycare(), c.getTemperament(), c.getFavoritePlay(),
				c.getFavoriteCharacter(), c.getHealthNote(), c.getHealthAgreedAt(), c.getMotivation());
		} else if (consultation instanceof QuoteConsultation c) {
			quote = new Quote(c.getInstitutionName(), c.isScheduleUndecided(), c.getEventDate(), c.getEventTime(),
				c.getVenueType(), c.getAddress(), c.getParticipantGroups(), c.getChildCount(), c.getGuardianCount(),
				c.getPrograms(), c.getNote());
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
			personal,
			quote
		);
	}
}
