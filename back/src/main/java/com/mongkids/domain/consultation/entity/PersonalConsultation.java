package com.mongkids.domain.consultation.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "personal_consultation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonalConsultation extends Consultation {

	@Column(nullable = false, length = 50)
	private String guardianName;

	@Column(nullable = false, length = 50)
	private String childName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "varchar(10)")
	private Gender childGender;

	private LocalDate childBirthDate;

	private Integer childAgeMonths;

	@Column(nullable = false, length = 255)
	private String address;

	@Column(nullable = false)
	private boolean attendsDaycare;

	@Column(columnDefinition = "TEXT")
	private String temperament;

	@Column(columnDefinition = "TEXT")
	private String favoritePlay;

	@Column(columnDefinition = "TEXT")
	private String favoriteCharacter;

	@Column(columnDefinition = "TEXT")
	private String healthNote;

	private LocalDateTime healthAgreedAt;

	@Column(columnDefinition = "TEXT")
	private String motivation;

	@Builder
	private PersonalConsultation(String phone, LocalDateTime privacyAgreedAt, String guardianName, String childName,
		Gender childGender, LocalDate childBirthDate, Integer childAgeMonths, String address, boolean attendsDaycare,
		String temperament, String favoritePlay, String favoriteCharacter, String healthNote,
		LocalDateTime healthAgreedAt, String motivation) {
		super(ConsultationType.PERSONAL, phone, privacyAgreedAt);
		this.guardianName = guardianName;
		this.childName = childName;
		this.childGender = childGender;
		this.childBirthDate = childBirthDate;
		this.childAgeMonths = childAgeMonths;
		this.address = address;
		this.attendsDaycare = attendsDaycare;
		this.temperament = temperament;
		this.favoritePlay = favoritePlay;
		this.favoriteCharacter = favoriteCharacter;
		this.healthNote = healthNote;
		this.healthAgreedAt = healthAgreedAt;
		this.motivation = motivation;
	}
}
