package com.mongkids.domain.classplan.service;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.mongkids.domain.classplan.dto.ClassPlanCreateRequest;
import com.mongkids.domain.classplan.dto.ClassPlanFileUploadUrlRequest;
import com.mongkids.domain.classplan.dto.ClassPlanFileUploadUrlRequest.Target;
import com.mongkids.domain.classplan.dto.ClassPlanFileUploadUrlResponse;
import com.mongkids.domain.classplan.dto.ClassPlanResponse;
import com.mongkids.domain.classplan.dto.ClassPlanUpdateRequest;
import com.mongkids.domain.classplan.entity.ClassPlan;
import com.mongkids.domain.classplan.repository.ClassPlanRepository;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;
import com.mongkids.global.storage.FileStorage;
import com.mongkids.global.storage.PresignedUpload;
import com.mongkids.global.storage.StoredFile;

import lombok.RequiredArgsConstructor;

/**
 * 수업 계획안 등록·열람 (SCR-027, FR-032, ADR-009).
 * 연간·월간 모두 관리자가 블러 처리한 공개 PDF와 원본 PDF 두 파일이다 (연간 포함은 2026-11-26).
 * 공개 PDF는 누구나, 원본은 기관 인증 방법이 정해지기 전까지 관리자만 받는다.
 * 2026-11-26 이전에 올린 연간 계획안은 원본이 없을 수 있고, 수정으로 원본을 붙인다.
 * PDF는 공개 CloudFront URL로 내보내지 않고 서버가 읽어 같은 출처로 전달한다 (파일 키가 응답에 나가지 않는다).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassPlanService {

	static final String PUBLIC_KEY_PREFIX = "plans/public/";
	static final String ORIGINAL_KEY_PREFIX = "plans/original/";
	// 계획안 PDF 한 파일의 최대 크기 (가정값, 대표 확인 전)
	static final long MAX_FILE_SIZE = 20L * 1024 * 1024;
	private static final String PDF_CONTENT_TYPE = "application/pdf";

	private final ClassPlanRepository classPlanRepository;
	private final MemberService memberService;
	private final FileStorage fileStorage;

	public List<ClassPlanResponse> getClassPlans() {
		return classPlanRepository.findAllByOrderByKindAscIdAsc().stream()
			.map(ClassPlanResponse::from)
			.toList();
	}

	/** 누구나 보는 블러본 PDF */
	public StoredFile openFile(Long classPlanId) {
		return openStored(getClassPlan(classPlanId).getFileKey());
	}

	/** 원본 PDF. 기관 인증 방법이 정해지기 전까지 관리자만 받는다 (FR-032) */
	public StoredFile openOriginalFile(Long adminId, Long classPlanId) {
		memberService.validateAdmin(adminId);
		ClassPlan classPlan = getClassPlan(classPlanId);
		if (classPlan.getOriginalFileKey() == null) {
			throw new BusinessException(ErrorCode.CLASS_PLAN_FILE_NOT_FOUND);
		}
		return openStored(classPlan.getOriginalFileKey());
	}

	public List<ClassPlanFileUploadUrlResponse> createFileUploadUrls(Long adminId,
		ClassPlanFileUploadUrlRequest request) {
		memberService.validateAdmin(adminId);
		return request.files().stream()
			.map(file -> {
				if (!PDF_CONTENT_TYPE.equals(file.contentType()) || file.size() <= 0 || file.size() > MAX_FILE_SIZE) {
					throw new BusinessException(ErrorCode.INVALID_CLASS_PLAN_FILE);
				}
				String fileKey = newFileKey(file.target());
				PresignedUpload upload = fileStorage.createUploadUrl(fileKey, PDF_CONTENT_TYPE, file.size());
				return new ClassPlanFileUploadUrlResponse(fileKey, upload.uploadUrl());
			})
			.toList();
	}

	@Transactional
	public ClassPlanResponse createClassPlan(Long adminId, ClassPlanCreateRequest request) {
		memberService.validateAdmin(adminId);
		String originalFileKey = request.originalFileKey();
		if (!StringUtils.hasText(originalFileKey)) {
			throw new BusinessException(ErrorCode.CLASS_PLAN_ORIGINAL_REQUIRED);
		}
		validateUploaded(request.fileKey(), PUBLIC_KEY_PREFIX);
		validateUploaded(originalFileKey, ORIGINAL_KEY_PREFIX);

		ClassPlan classPlan = classPlanRepository.saveAndFlush(ClassPlan.builder()
			.kind(request.kind())
			.title(request.title().strip())
			.period(request.period().strip())
			.fileKey(request.fileKey())
			.originalFileKey(originalFileKey)
			.build());
		return ClassPlanResponse.from(classPlan);
	}

	/**
	 * 제목·기간을 바꾸고, 새 파일 키가 온 파일만 교체한다. 교체·삭제로 빠진 S3 객체는 지우지 않는다(다른 사진과 같은 정책).
	 */
	@Transactional
	public ClassPlanResponse updateClassPlan(Long adminId, Long classPlanId, ClassPlanUpdateRequest request) {
		memberService.validateAdmin(adminId);
		ClassPlan classPlan = getClassPlan(classPlanId);
		boolean newOriginal = StringUtils.hasText(request.originalFileKey());
		if (StringUtils.hasText(request.fileKey())) {
			validateUploaded(request.fileKey(), PUBLIC_KEY_PREFIX);
			classPlan.changeFile(request.fileKey());
		}
		if (newOriginal) {
			validateUploaded(request.originalFileKey(), ORIGINAL_KEY_PREFIX);
			classPlan.changeOriginalFile(request.originalFileKey());
		}
		classPlan.changeInfo(request.title().strip(), request.period().strip());
		// 응답의 updatedAt이 바뀐 시각이 되도록 먼저 반영한다 (화면이 이 값으로 PDF를 새로 받는다)
		classPlanRepository.flush();
		return ClassPlanResponse.from(classPlan);
	}

	@Transactional
	public void deleteClassPlan(Long adminId, Long classPlanId) {
		memberService.validateAdmin(adminId);
		classPlanRepository.delete(getClassPlan(classPlanId));
	}

	private ClassPlan getClassPlan(Long classPlanId) {
		return classPlanRepository.findById(classPlanId)
			.orElseThrow(() -> new BusinessException(ErrorCode.CLASS_PLAN_NOT_FOUND));
	}

	private StoredFile openStored(String key) {
		return fileStorage.open(key)
			.orElseThrow(() -> new BusinessException(ErrorCode.CLASS_PLAN_FILE_NOT_FOUND));
	}

	/**
	 * 업로드 URL로 실제 올라간 계획안 PDF인지 확인한다. 공개본 자리에 원본 키를 넣는 실수도 경로로 막는다.
	 */
	private void validateUploaded(String fileKey, String prefix) {
		if (!fileKey.startsWith(prefix)) {
			throw new BusinessException(ErrorCode.INVALID_CLASS_PLAN_FILE);
		}
		long size = fileStorage.findSize(fileKey)
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CLASS_PLAN_FILE));
		if (size > MAX_FILE_SIZE) {
			throw new BusinessException(ErrorCode.INVALID_CLASS_PLAN_FILE);
		}
	}

	private static String newFileKey(Target target) {
		YearMonth now = YearMonth.now();
		String prefix = target == Target.ORIGINAL ? ORIGINAL_KEY_PREFIX : PUBLIC_KEY_PREFIX;
		return "%s%d/%02d/%s.pdf".formatted(prefix, now.getYear(), now.getMonthValue(), UUID.randomUUID());
	}
}
