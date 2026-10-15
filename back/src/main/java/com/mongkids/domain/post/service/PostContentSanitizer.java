package com.mongkids.domain.post.service;

import java.util.List;
import java.util.function.UnaryOperator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

import com.mongkids.global.exception.BusinessException;
import com.mongkids.global.exception.ErrorCode;

/**
 * 게시물 본문 HTML을 허용 목록으로 정화하고 구조를 검사한다 (FR-008).
 * 사진은 {@code <figure class="post-photos">} 안의 {@code <img data-image-key>}로만 들어오고,
 * DB에는 src 없이 키만 저장한 뒤 응답 때 URL을 채운다.
 */
@Component
public class PostContentSanitizer {

	static final int MAX_CONTENT_LENGTH = 100_000;
	static final int MAX_PHOTOS_PER_GROUP = 3;
	static final int MAX_CAPTION_LENGTH = 200;

	private static final String PHOTO_GROUP_CLASS = "post-photos";
	private static final String IMAGE_KEY_ATTR = "data-image-key";

	private static final Safelist SAFELIST = new Safelist()
		.addTags("p", "br", "strong", "em", "u", "a", "ul", "ol", "li", "blockquote", "hr",
			"figure", "figcaption", "img")
		.addAttributes("a", "href")
		.addProtocols("a", "href", "http", "https")
		.addAttributes("figure", "class")
		.addAttributes("img", IMAGE_KEY_ATTR);

	/**
	 * 정화된 HTML과 본문에 나온 순서대로의 사진 키.
	 */
	public record SanitizedContent(String html, List<String> imageKeys) {
	}

	public SanitizedContent sanitize(String html) {
		if (html.length() > MAX_CONTENT_LENGTH) {
			throw new BusinessException(ErrorCode.INVALID_POST_CONTENT);
		}
		Document document = parse(Jsoup.clean(html, "", SAFELIST, new Document.OutputSettings().prettyPrint(false)));

		for (Element figure : document.select("figure")) {
			validatePhotoGroup(figure);
		}
		List<Element> images = document.select("img");
		for (Element image : images) {
			Element parent = image.parent();
			if (parent == null || !isPhotoGroup(parent) || image.attr(IMAGE_KEY_ATTR).isBlank()) {
				throw new BusinessException(ErrorCode.INVALID_POST_CONTENT);
			}
		}
		if (document.body().text().isBlank() && images.isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_POST_CONTENT);
		}
		List<String> imageKeys = images.stream().map(image -> image.attr(IMAGE_KEY_ATTR)).toList();
		return new SanitizedContent(document.body().html(), imageKeys);
	}

	/**
	 * 저장된 HTML을 응답용으로 바꾼다: 사진에 URL을 채우고 링크는 새 창으로 연다.
	 */
	public String render(String html, UnaryOperator<String> imageUrlResolver) {
		Document document = parse(html);
		for (Element image : document.select("img[" + IMAGE_KEY_ATTR + "]")) {
			image.attr("src", imageUrlResolver.apply(image.attr(IMAGE_KEY_ATTR))).attr("alt", "");
		}
		for (Element link : document.select("a[href]")) {
			link.attr("target", "_blank").attr("rel", "noopener noreferrer");
		}
		return document.body().html();
	}

	private void validatePhotoGroup(Element figure) {
		if (!isPhotoGroup(figure)) {
			throw new BusinessException(ErrorCode.INVALID_POST_CONTENT);
		}
		int imageCount = figure.select("> img").size();
		List<Element> captions = figure.select("> figcaption");
		boolean onlyPhotosAndCaption = figure.children().size() == imageCount + captions.size();
		if (imageCount < 1 || imageCount > MAX_PHOTOS_PER_GROUP || captions.size() > 1 || !onlyPhotosAndCaption
			|| captions.stream().anyMatch(caption -> caption.text().length() > MAX_CAPTION_LENGTH)) {
			throw new BusinessException(ErrorCode.INVALID_POST_CONTENT);
		}
	}

	private static boolean isPhotoGroup(Element element) {
		return element.nameIs("figure") && PHOTO_GROUP_CLASS.equals(element.attr("class"));
	}

	private static Document parse(String html) {
		Document document = Jsoup.parseBodyFragment(html);
		document.outputSettings().prettyPrint(false);
		return document;
	}
}
