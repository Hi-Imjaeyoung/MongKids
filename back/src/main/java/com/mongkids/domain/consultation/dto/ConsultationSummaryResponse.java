package com.mongkids.domain.consultation.dto;

import java.time.LocalDateTime;

import com.mongkids.domain.consultation.entity.Consultation;
import com.mongkids.domain.consultation.entity.ConsultationStatus;
import com.mongkids.domain.consultation.entity.ConsultationType;
import com.mongkids.domain.consultation.entity.InstitutionConsultation;
import com.mongkids.domain.consultation.entity.PersonalConsultation;
import com.mongkids.domain.consultation.entity.QuoteConsultation;

/**
 * 관리자 상담 신청 목록 한 줄 (FR-004). {@code title}은 기관명, 보호자 이름 또는 견적을 요청한 기관·단체명이다.
 */
public record ConsultationSummaryResponse(
	Long id,
	ConsultationType type,
	String title,
	String phone,
	ConsultationStatus status,
	LocalDateTime createdAt
) {

	public static ConsultationSummaryResponse from(Consultation consultation) {
		return new ConsultationSummaryResponse(
			consultation.getId(),
			consultation.getType(),
			title(consultation),
			consultation.getPhone(),
			consultation.getStatus(),
			consultation.getCreatedAt()
		);
	}

	private static String title(Consultation consultation) {
		if (consultation instanceof InstitutionConsultation institution) {
			return institution.getInstitutionName();
		}
		if (consultation instanceof PersonalConsultation personal) {
			return personal.getGuardianName();
		}
		if (consultation instanceof QuoteConsultation quote) {
			return quote.getInstitutionName();
		}
		throw new IllegalStateException("알 수 없는 상담 유형: " + consultation.getType());
	}
}
