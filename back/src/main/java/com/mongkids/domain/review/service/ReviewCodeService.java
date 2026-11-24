package com.mongkids.domain.review.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.mongkids.domain.member.service.MemberService;
import com.mongkids.domain.review.dto.ReviewCodeCreateRequest;
import com.mongkids.domain.review.dto.ReviewCodeResponse;
import com.mongkids.domain.review.entity.ReviewCode;
import com.mongkids.domain.review.entity.ReviewCodeKind;
import com.mongkids.domain.review.entity.ReviewCodeStatus;
import com.mongkids.domain.review.repository.ReviewCodeRepository;
import com.mongkids.global.common.PageResponse;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 후기 작성 코드 발급·목록·폐기 (FR-029, ADR-005). 관리자만 쓴다.
 * 코드는 {@code MK-XXXX-XXXX} 형식으로, 헷갈리는 글자(0·O·1·I)를 뺀 32글자에서 SecureRandom으로 고른다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewCodeService {

	static final int MAX_PAGE_SIZE = 100;
	static final int MAX_EXPIRY_DAYS = 365;
	static final Sort LATEST = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
	private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
	private static final int MAX_GENERATE_ATTEMPTS = 5;
	private static final SecureRandom RANDOM = new SecureRandom();

	private final ReviewCodeRepository reviewCodeRepository;
	private final MemberService memberService;
	private final Clock clock;

	/**
	 * 최근 발급 순 목록. active가 null이면 전체, true면 사용 가능한 코드만, false면 끝난 코드만.
	 */
	public PageResponse<ReviewCodeResponse> getReviewCodes(Long adminId, Boolean active, Pageable pageable) {
		memberService.validateAdmin(adminId);
		LocalDate today = LocalDate.now(clock);
		Pageable latest = PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), MAX_PAGE_SIZE),
			LATEST);
		return PageResponse.from(reviewCodeRepository.searchForAdmin(active, today, latest),
			reviewCode -> ReviewCodeResponse.of(reviewCode, today));
	}

	/**
	 * 만료일은 오늘부터 1년 안. 1회용은 사용 횟수 1로 저장하고 메모를 받지 않는다. 묶음은 사용 횟수가 필요하다.
	 */
	@Transactional
	public ReviewCodeResponse createReviewCode(Long adminId, ReviewCodeCreateRequest request) {
		memberService.validateAdmin(adminId);
		LocalDate today = LocalDate.now(clock);
		if (request.expiresOn().isBefore(today) || request.expiresOn().isAfter(today.plusDays(MAX_EXPIRY_DAYS))) {
			throw new BusinessException(ErrorCode.INVALID_REVIEW_CODE_OPTION);
		}
		boolean bundle = request.kind() == ReviewCodeKind.BUNDLE;
		if (bundle && request.maxUses() == null) {
			throw new BusinessException(ErrorCode.INVALID_REVIEW_CODE_OPTION);
		}

		ReviewCode reviewCode = reviewCodeRepository.save(ReviewCode.builder()
			.code(generateCode())
			.kind(request.kind())
			.maxUses(bundle ? request.maxUses() : 1)
			.expiresOn(request.expiresOn())
			.memo(bundle && StringUtils.hasText(request.memo()) ? request.memo().strip() : null)
			.build());
		return ReviewCodeResponse.of(reviewCode, today);
	}

	/**
	 * 사용 가능한 코드만 폐기한다. 이미 쓴 후기는 그대로 둔다.
	 */
	@Transactional
	public ReviewCodeResponse revokeReviewCode(Long adminId, Long reviewCodeId) {
		memberService.validateAdmin(adminId);
		ReviewCode reviewCode = reviewCodeRepository.findById(reviewCodeId)
			.orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_CODE_NOT_FOUND));
		LocalDate today = LocalDate.now(clock);
		if (reviewCode.status(today) != ReviewCodeStatus.ACTIVE) {
			throw new BusinessException(ErrorCode.REVIEW_CODE_NOT_ACTIVE);
		}
		reviewCode.revoke(LocalDateTime.now(clock));
		return ReviewCodeResponse.of(reviewCode, today);
	}

	private String generateCode() {
		for (int attempt = 0; attempt < MAX_GENERATE_ATTEMPTS; attempt++) {
			String code = "MK-" + randomPart() + "-" + randomPart();
			if (!reviewCodeRepository.existsByCode(code)) {
				return code;
			}
		}
		throw new IllegalStateException("후기 작성 코드를 만들지 못했습니다.");
	}

	private static String randomPart() {
		StringBuilder part = new StringBuilder(4);
		for (int i = 0; i < 4; i++) {
			part.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
		}
		return part.toString();
	}
}
