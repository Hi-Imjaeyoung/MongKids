package com.mongkids.domain.content.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mongkids.domain.content.dto.AboutCurriculumMonthResponse;
import com.mongkids.domain.content.service.AboutCurriculumService;
import com.mongkids.global.common.ApiResponse;

import lombok.RequiredArgsConstructor;

// ABOUT 몽키즈(SCR-002) 월별 커리큘럼 조회 (FR-034). 인증 없이 호출한다
@RestController
@RequestMapping("/api/v1/about-curriculum-months")
@RequiredArgsConstructor
public class AboutCurriculumController {

	private final AboutCurriculumService aboutCurriculumService;

	@GetMapping
	public ApiResponse<List<AboutCurriculumMonthResponse>> getMonths() {
		return ApiResponse.success(aboutCurriculumService.getMonths());
	}
}
