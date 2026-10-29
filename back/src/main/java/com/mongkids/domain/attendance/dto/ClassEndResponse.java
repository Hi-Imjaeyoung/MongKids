package com.mongkids.domain.attendance.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.UnaryOperator;

import com.mongkids.domain.attendance.entity.ClassEnd;

/**
 * 수업 종료 기록 (EMP-FR-010). {@code correctable}은 작성자가 지금 정정할 수 있는지다 (EMP-FR-014).
 */
public record ClassEndResponse(
	LocalDateTime endedAt,
	LocalDateTime correctedAt,
	String note,
	List<ImageResponse> images,
	boolean correctable
) {

	public static ClassEndResponse of(ClassEnd end, UnaryOperator<String> imageUrlResolver, boolean correctable) {
		List<ImageResponse> images = end.getImages().stream()
			.map(image -> new ImageResponse(image.getImageKey(), imageUrlResolver.apply(image.getImageKey())))
			.toList();
		return new ClassEndResponse(end.getEndedAt(), end.getCorrectedAt(), end.getNote(), images, correctable);
	}

	public record ImageResponse(
		String imageKey,
		String url
	) {
	}
}
