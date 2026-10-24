package com.mongkids.domain.consultation.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongkids.domain.consultation.dto.ConsultationDetailResponse;
import com.mongkids.domain.consultation.dto.ConsultationStatusRequest;
import com.mongkids.domain.consultation.dto.ConsultationSummaryResponse;
import com.mongkids.domain.consultation.entity.ConsultationStatus;
import com.mongkids.domain.consultation.entity.ConsultationType;
import com.mongkids.domain.consultation.service.ConsultationService;
import com.mongkids.global.common.ApiResponse;
import com.mongkids.global.common.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 관리자 화면(SCR-014)의 상담 신청 조회(FR-004)와 처리 상태(FR-026)
@RestController
@RequestMapping("/api/v1/admin/consultations")
@RequiredArgsConstructor
public class AdminConsultationController {

	private final ConsultationService consultationService;

	@GetMapping
	public ApiResponse<PageResponse<ConsultationSummaryResponse>> getConsultations(
		@AuthenticationPrincipal Long memberId,
		@RequestParam(required = false) ConsultationType type,
		@RequestParam(required = false) ConsultationStatus status,
		@PageableDefault(size = 20) Pageable pageable
	) {
		return ApiResponse.success(consultationService.getConsultations(memberId, type, status, pageable));
	}

	@GetMapping("/{consultationId}")
	public ApiResponse<ConsultationDetailResponse> getConsultation(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long consultationId
	) {
		return ApiResponse.success(consultationService.getConsultation(memberId, consultationId));
	}

	@PutMapping("/{consultationId}/status")
	public ApiResponse<ConsultationDetailResponse> changeStatus(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long consultationId,
		@Valid @RequestBody ConsultationStatusRequest request
	) {
		return ApiResponse.success(consultationService.changeStatus(memberId, consultationId, request));
	}
}
