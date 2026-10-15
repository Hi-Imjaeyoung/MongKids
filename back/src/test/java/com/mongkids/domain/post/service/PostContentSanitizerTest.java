package com.mongkids.domain.post.service;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mongkids.domain.post.service.PostContentSanitizer.SanitizedContent;
import com.mongkids.global.exception.ErrorCode;

class PostContentSanitizerTest {

	private final PostContentSanitizer sanitizer = new PostContentSanitizer();

	@Test
	@DisplayName("허용한 서식과 사진 묶음은 그대로 남는다")
	void sanitize_keepsAllowedFormatting() {
		// given
		String html = "<p>오늘은 <strong>가을</strong> <em>운동회</em> <u>날</u><br>입니다</p>"
			+ "<ul><li>줄다리기</li></ul><ol><li>달리기</li></ol><blockquote><p>인용</p></blockquote><hr>"
			+ "<p><a href=\"https://mongkids.co.kr\">링크</a></p>"
			+ "<figure class=\"post-photos\"><img data-image-key=\"posts/a.jpg\"><figcaption>캡션</figcaption></figure>";

		// when
		SanitizedContent content = sanitizer.sanitize(html);

		// then
		assertThat(content.html()).isEqualTo(html);
	}

	@Test
	@DisplayName("스크립트·이벤트 속성·style·허용하지 않은 태그와 javascript 링크는 제거한다")
	void sanitize_removesDangerousMarkup() {
		// given
		String html = "<p onclick=\"alert(1)\" style=\"color:red\">본문<script>alert(1)</script></p>"
			+ "<h2>소제목</h2><iframe src=\"https://evil.test\"></iframe>"
			+ "<p><a href=\"javascript:alert(1)\">나쁜 링크</a></p>";

		// when
		SanitizedContent content = sanitizer.sanitize(html);

		// then
		assertThat(content.html())
			.doesNotContain("script", "onclick", "style", "h2", "iframe", "javascript")
			.contains("<p>본문</p>", "소제목", "나쁜 링크");
	}

	@Test
	@DisplayName("사진 키를 본문에 나온 순서대로 추출하고 src 등 다른 속성은 지운다")
	void sanitize_extractsImageKeysInOrder() {
		// given
		String html = "<figure class=\"post-photos\"><img data-image-key=\"posts/b.jpg\" src=\"https://evil.test/x.jpg\">"
			+ "<img data-image-key=\"posts/a.jpg\"></figure><p>글</p>"
			+ "<figure class=\"post-photos\"><img data-image-key=\"posts/c.jpg\"></figure>";

		// when
		SanitizedContent content = sanitizer.sanitize(html);

		// then
		assertThat(content.imageKeys()).containsExactly("posts/b.jpg", "posts/a.jpg", "posts/c.jpg");
		assertThat(content.html()).doesNotContain("src=");
	}

	@Test
	@DisplayName("사진 묶음 밖의 사진, 4장 묶음, 긴 캡션, 다른 class의 figure, 빈 본문은 거부한다")
	void sanitize_invalidStructure_throwsException() {
		String longCaption = "가".repeat(PostContentSanitizer.MAX_CAPTION_LENGTH + 1);
		String fourPhotos = "<img data-image-key=\"posts/1.jpg\">".repeat(PostContentSanitizer.MAX_PHOTOS_PER_GROUP + 1);
		String[] invalidContents = {
			"<p>글<img data-image-key=\"posts/a.jpg\"></p>",
			"<figure class=\"post-photos\">" + fourPhotos + "</figure>",
			"<figure class=\"post-photos\"><img data-image-key=\"posts/a.jpg\"><figcaption>" + longCaption
				+ "</figcaption></figure>",
			"<figure class=\"other\"><img data-image-key=\"posts/a.jpg\"></figure>",
			"<figure class=\"post-photos\"><figcaption>사진 없음</figcaption></figure>",
			"<figure class=\"post-photos\"><img></figure>",
			"<p></p>",
			"<script>alert(1)</script>",
		};

		for (String html : invalidContents) {
			assertThatThrownBy(() -> sanitizer.sanitize(html))
				.as(html)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.INVALID_POST_CONTENT);
		}
	}

	@Test
	@DisplayName("본문이 너무 길면 거부한다")
	void sanitize_tooLong_throwsException() {
		String html = "<p>" + "가".repeat(PostContentSanitizer.MAX_CONTENT_LENGTH) + "</p>";

		assertThatThrownBy(() -> sanitizer.sanitize(html))
			.extracting("errorCode")
			.isEqualTo(ErrorCode.INVALID_POST_CONTENT);
	}

	@Test
	@DisplayName("응답용 HTML은 사진에 URL을 채우고 링크를 새 창으로 연다")
	void render_fillsImageUrlAndLinkTarget() {
		// given
		String html = "<p><a href=\"https://mongkids.co.kr\">링크</a></p>"
			+ "<figure class=\"post-photos\"><img data-image-key=\"posts/a.jpg\"></figure>";

		// when
		String rendered = sanitizer.render(html, key -> "https://cdn.test/" + key);

		// then
		assertThat(rendered)
			.contains("src=\"https://cdn.test/posts/a.jpg\"")
			.contains("target=\"_blank\"", "rel=\"noopener noreferrer\"");
	}
}
