package com.mongkids.domain.classplan.controller;

import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mongkids.domain.classplan.dto.ClassPlanResponse;
import com.mongkids.domain.classplan.service.ClassPlanService;
import com.mongkids.global.common.ApiResponse;

import lombok.RequiredArgsConstructor;

// 수업 계획안(SCR-027) 목록과 공개 PDF (FR-032). 인증 없이 호출한다
@RestController
@RequestMapping("/api/v1/class-plans")
@RequiredArgsConstructor
public class ClassPlanController {

	private final ClassPlanService classPlanService;

	@GetMapping
	public ApiResponse<List<ClassPlanResponse>> getClassPlans() {
		return ApiResponse.success(classPlanService.getClassPlans());
	}

	// 연간 계획안·월간 블러본. 파일을 바꾸면 화면이 updatedAt을 주소에 붙여 새로 받는다
	@GetMapping("/{classPlanId}/file")
	public ResponseEntity<InputStreamResource> getFile(@PathVariable Long classPlanId) {
		return ClassPlanPdf.of(classPlanService.openFile(classPlanId), CacheControl.noCache());
	}
}
