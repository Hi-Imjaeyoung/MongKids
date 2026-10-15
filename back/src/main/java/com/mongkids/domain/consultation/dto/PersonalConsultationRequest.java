package com.mongkids.domain.consultation.dto;

import java.time.LocalDate;

import com.mongkids.domain.consultation.entity.Gender;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 개인 상담 신청 요청 (FR-002). 비회원도 보낼 수 있고(FR-003), 개인정보 수집 동의가 필수다(FR-023).
 * 생년월일·개월 수는 둘 중 하나만, 건강 메모를 적으면 건강정보 동의가 필요하다 (Service 검증).
 */
public record PersonalConsultationRequest(
	@NotBlank @Size(max = 50) String guardianName,
	// 번호 인증을 하지 않아 휴대전화(010)만 받는다. 화면은 010을 고정하고 8자리만 입력받는다
	@NotBlank @Pattern(regexp = "^010-?\\d{4}-?\\d{4}$") String phone,
	@NotBlank @Size(max = 50) String childName,
	@NotNull Gender childGender,
	@PastOrPresent LocalDate childBirthDate,
	@Min(0) @Max(120) Integer childAgeMonths,
	@NotBlank @Size(max = 255) String address,
	@NotNull Boolean attendsDaycare,
	@Size(max = 1000) String temperament,
	@Size(max = 1000) String favoritePlay,
	@Size(max = 1000) String favoriteCharacter,
	@Size(max = 1000) String healthNote,
	@Size(max = 1000) String motivation,
	@AssertTrue boolean privacyAgreed,
	// 건강 메모가 없으면 보내지 않아도 된다 (null은 미동의)
	Boolean healthAgreed
) {
}
