package com.mongkids.domain.post.service;

import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Component;

import com.mongkids.domain.member.entity.Member;
import com.mongkids.domain.member.service.MemberService;
import com.mongkids.domain.post.entity.Article;
import com.mongkids.domain.post.service.PostContentSanitizer.SanitizedContent;
import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;
import com.mongkids.global.storage.FileStorage;

import lombok.RequiredArgsConstructor;

/**
 * 수업 현장 게시물과 몽키즈 연구소 글이 함께 쓰는 쓰기 규칙: 권한(FR-009), 본문 정화, 사진 제한(FR-008).
 */
@Component
@RequiredArgsConstructor
public class ArticleWriteSupport {

	static final int MAX_IMAGE_COUNT = 20;
	static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;
	static final String IMAGE_KEY_PREFIX = "posts/";

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

	public void validateImageCount(int count) {
		if (count > MAX_IMAGE_COUNT) {
			throw new BusinessException(ErrorCode.POST_IMAGE_LIMIT_EXCEEDED);
		}
	}

	/**
	 * 본문을 정화하고 본문 속 사진이 실제 올라간 게시물 사진인지 확인한다.
	 */
	public SanitizedContent sanitizeContent(String html) {
		SanitizedContent content = contentSanitizer.sanitize(html);
		validateImageKeys(content.imageKeys());
		return content;
	}

	/**
	 * 업로드 URL로 실제 올라간 게시물 사진인지 확인한다.
	 */
	private void validateImageKeys(List<String> imageKeys) {
		validateImageCount(imageKeys.size());
		if (new HashSet<>(imageKeys).size() != imageKeys.size()) {
			throw new BusinessException(ErrorCode.INVALID_POST_IMAGE);
		}
		for (String imageKey : imageKeys) {
			if (!imageKey.startsWith(IMAGE_KEY_PREFIX)) {
				throw new BusinessException(ErrorCode.INVALID_POST_IMAGE);
			}
			long size = fileStorage.findSize(imageKey)
				.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_POST_IMAGE));
			if (size > MAX_IMAGE_SIZE) {
				throw new BusinessException(ErrorCode.INVALID_POST_IMAGE);
			}
		}
	}
}
