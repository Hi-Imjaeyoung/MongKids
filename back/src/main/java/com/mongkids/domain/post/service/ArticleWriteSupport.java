package com.mongkids.domain.post.service;

import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.domain.post.dto.PostImageUploadUrlRequest;
import com.mongkids.domain.post.dto.PostImageUploadUrlResponse;
import com.mongkids.domain.post.entity.Article;
import com.mongkids.domain.post.entity.ArticleImage;
import com.mongkids.domain.post.service.PostContentSanitizer.SanitizedContent;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;
import com.mongkids.global.storage.FileStorage;
import com.mongkids.global.storage.PresignedUpload;

import lombok.RequiredArgsConstructor;

/**
 * 수업 현장 게시물과 몽키즈 연구소 글이 함께 쓰는 쓰기 규칙: 권한(FR-009), 본문 정화, 사진 제한(FR-008).
 * 사진 키는 묶음별 경로(수업 현장 {@code posts/}, 연구소 {@code lab/})에 나눠 둔다. 경로마다 사진 테이블이 하나씩 대응한다.
 */
@Component
@RequiredArgsConstructor
public class ArticleWriteSupport {

	static final int MAX_IMAGE_COUNT = 20;
	static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;
	static final String POST_IMAGE_KEY_PREFIX = "posts/";
	static final String LAB_IMAGE_KEY_PREFIX = "lab/";

	private static final Map<String, String> IMAGE_EXTENSIONS = Map.of(
		"image/jpeg", "jpg",
		"image/png", "png",
		"image/webp", "webp"
	);

	private final MemberService memberService;
	private final FileStorage fileStorage;
	private final PostContentSanitizer contentSanitizer;

	/**
	 * 글을 쓸 수 있는 직원(STAFF·ADMIN)인지 DB의 현재 역할로 확인한다 (FR-009).
	 * 토큰의 역할을 쓰지 않으므로 권한을 회수하면 다시 로그인하지 않아도 바로 막힌다.
	 */
	public Member getWriter(Long memberId) {
		Member member = memberService.getActiveMember(memberId);
		if (!member.getRole().canWritePost()) {
			throw new BusinessException(ErrorCode.FORBIDDEN);
		}
		return member;
	}

	/**
	 * 수정·삭제할 수 있는 글인지 확인한다: 직원 권한이 있는 작성자 본인이거나 관리자.
	 *
	 * @param writer {@link #getWriter}로 확인한 직원
	 */
	public void checkWritable(Member writer, Article article) {
		if (!article.getMember().getId().equals(writer.getId()) && !writer.getRole().isAdmin()) {
			throw new BusinessException(ErrorCode.FORBIDDEN);
		}
	}

	/**
	 * 사진을 S3에 직접 올릴 URL을 발급한다 (FR-008: 최대 20장, 장당 10MB). 글을 쓸 수 있는 직원만 (FR-009).
	 *
	 * @param keyPrefix 묶음의 사진 경로 ({@link #POST_IMAGE_KEY_PREFIX}, {@link #LAB_IMAGE_KEY_PREFIX})
	 */
	public List<PostImageUploadUrlResponse> createImageUploadUrls(Long memberId, PostImageUploadUrlRequest request,
		String keyPrefix) {
		getWriter(memberId);
		validateImageCount(request.files().size());
		return request.files().stream()
			.map(file -> {
				String extension = IMAGE_EXTENSIONS.get(file.contentType());
				if (extension == null || file.size() <= 0 || file.size() > MAX_IMAGE_SIZE) {
					throw new BusinessException(ErrorCode.INVALID_POST_IMAGE);
				}
				String imageKey = newImageKey(keyPrefix, extension);
				PresignedUpload upload = fileStorage.createUploadUrl(imageKey, file.contentType(), file.size());
				return new PostImageUploadUrlResponse(imageKey, upload.uploadUrl());
			})
			.toList();
	}

	public void validateImageCount(int count) {
		if (count > MAX_IMAGE_COUNT) {
			throw new BusinessException(ErrorCode.POST_IMAGE_LIMIT_EXCEEDED);
		}
	}

	/**
	 * 새 글의 본문을 정화하고 본문 속 사진이 이 묶음 경로에 실제 올라간 사진인지 확인한다.
	 */
	public SanitizedContent sanitizeContent(String html, String keyPrefix) {
		return sanitizeContent(html, keyPrefix, Set.of());
	}

	/**
	 * 수정할 글의 본문을 정화한다. 글에 이미 붙어 있는 사진은 경로가 달라도 그대로 둘 수 있다.
	 * 경로를 나누기 전({@code posts/})에 올린 연구소 글 사진을 수정 때 거부하지 않기 위해서다.
	 */
	public SanitizedContent sanitizeContent(String html, String keyPrefix, Article current) {
		Set<String> currentKeys = current.getImages().stream()
			.map(ArticleImage::getImageKey)
			.collect(Collectors.toSet());
		return sanitizeContent(html, keyPrefix, currentKeys);
	}

	private SanitizedContent sanitizeContent(String html, String keyPrefix, Set<String> currentKeys) {
		SanitizedContent content = contentSanitizer.sanitize(html);
		validateImageKeys(content.imageKeys(), keyPrefix, currentKeys);
		return content;
	}

	/**
	 * 업로드 URL로 이 묶음 경로에 실제 올라간 사진인지 확인한다.
	 */
	private void validateImageKeys(List<String> imageKeys, String keyPrefix, Set<String> currentKeys) {
		validateImageCount(imageKeys.size());
		if (new HashSet<>(imageKeys).size() != imageKeys.size()) {
			throw new BusinessException(ErrorCode.INVALID_POST_IMAGE);
		}
		for (String imageKey : imageKeys) {
			if (!imageKey.startsWith(keyPrefix) && !currentKeys.contains(imageKey)) {
				throw new BusinessException(ErrorCode.INVALID_POST_IMAGE);
			}
			long size = fileStorage.findSize(imageKey)
				.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_POST_IMAGE));
			if (size > MAX_IMAGE_SIZE) {
				throw new BusinessException(ErrorCode.INVALID_POST_IMAGE);
			}
		}
	}

	private static String newImageKey(String keyPrefix, String extension) {
		YearMonth now = YearMonth.now();
		return "%s%d/%02d/%s.%s".formatted(keyPrefix, now.getYear(), now.getMonthValue(), UUID.randomUUID(), extension);
	}
}
