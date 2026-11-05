package com.mongkids.domain.consultation.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 운동회 견적 신청 (FR-031). 상담 신청의 세 번째 유형으로 목록·처리 상태·알림·보관 기간을 공유한다
@Entity
@Table(name = "quote_consultation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuoteConsultation extends Consultation {

	@Column(nullable = false, length = 100)
	private String institutionName;

	// 일정 미정이면 날짜·시간은 NULL
	@Column(nullable = false)
	private boolean scheduleUndecided;

	private LocalDate eventDate;

	private LocalTime eventTime;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "varchar(20)")
	private VenueType venueType;

	// 장소 미정이면 NULL
	@Column(length = 255)
	private String address;

	@Convert(converter = ParticipantGroupListConverter.class)
	@Column(nullable = false, length = 100)
	private List<ParticipantGroup> participantGroups = new ArrayList<>();

	// 아동·보호자 인원 중 하나 이상 (Service 검증)
	private Integer childCount;

	private Integer guardianCount;

	@Convert(converter = QuoteProgramListConverter.class)
	@Column(length = 100)
	private List<QuoteProgram> programs = new ArrayList<>();

	@Column(columnDefinition = "TEXT")
	private String note;

	@Builder
	private QuoteConsultation(String phone, LocalDateTime privacyAgreedAt, String institutionName,
		boolean scheduleUndecided, LocalDate eventDate, LocalTime eventTime, VenueType venueType, String address,
		List<ParticipantGroup> participantGroups, Integer childCount, Integer guardianCount,
		List<QuoteProgram> programs, String note) {
		super(ConsultationType.QUOTE, phone, privacyAgreedAt);
		this.institutionName = institutionName;
		this.scheduleUndecided = scheduleUndecided;
		this.eventDate = eventDate;
		this.eventTime = eventTime;
		this.venueType = venueType;
		this.address = address;
		this.participantGroups = List.copyOf(participantGroups);
		this.childCount = childCount;
		this.guardianCount = guardianCount;
		this.programs = programs == null ? List.of() : List.copyOf(programs);
		this.note = note;
	}
}
