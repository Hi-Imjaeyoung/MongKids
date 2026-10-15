package com.mongkids.domain.consultation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mongkids.domain.consultation.dto.ConsultationCreateResponse;
import com.mongkids.domain.consultation.dto.InstitutionConsultationRequest;
import com.mongkids.domain.consultation.dto.PersonalConsultationRequest;
import com.mongkids.domain.consultation.service.ConsultationService;
import com.mongkids.global.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ConsultationController {

	private final ConsultationService consultationService;

	@PostMapping("/api/v1/institution-consultations")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<ConsultationCreateResponse> createInstitutionConsultation(
		@Valid @RequestBody InstitutionConsultationRequest request
	) {
		return ApiResponse.success(consultationService.createInstitutionConsultation(request));
	}

	@PostMapping("/api/v1/personal-consultations")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<ConsultationCreateResponse> createPersonalConsultation(
		@Valid @RequestBody PersonalConsultationRequest request
	) {
		return ApiResponse.success(consultationService.createPersonalConsultation(request));
	}
}
