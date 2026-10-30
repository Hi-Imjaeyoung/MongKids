package com.mongkids.domain.attendance.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mongkids.domain.attendance.dto.InstitutionRequest;
import com.mongkids.domain.attendance.dto.InstitutionResponse;
import com.mongkids.domain.attendance.service.InstitutionService;
import com.mongkids.global.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// 기관 위치 등록 (EMP-FR-012, EMP-SCR-006)
@RestController
@RequestMapping("/api/v1/admin/institutions")
@RequiredArgsConstructor
public class AdminInstitutionController {

	private final InstitutionService institutionService;

	@GetMapping
	public ApiResponse<List<InstitutionResponse>> getInstitutions(@AuthenticationPrincipal Long memberId) {
		return ApiResponse.success(institutionService.getInstitutions(memberId));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<InstitutionResponse> createInstitution(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody InstitutionRequest request
	) {
		return ApiResponse.success(institutionService.createInstitution(memberId, request));
	}

	@PutMapping("/{institutionId}")
	public ApiResponse<InstitutionResponse> updateInstitution(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long institutionId,
		@Valid @RequestBody InstitutionRequest request
	) {
		return ApiResponse.success(institutionService.updateInstitution(memberId, institutionId, request));
	}

	@DeleteMapping("/{institutionId}")
	public ApiResponse<Void> deleteInstitution(@AuthenticationPrincipal Long memberId,
		@PathVariable Long institutionId) {
		institutionService.deleteInstitution(memberId, institutionId);
		return ApiResponse.success(null);
	}
}
