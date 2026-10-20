package com.mongkids.domain.post.entity;

import com.mongkids.global.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 글 사진의 공통 부모. 본문에 처음 나온 사진(0번)이 대표 사진이다.
 */
@MappedSuperclass
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ArticleImage extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 255)
	private String imageKey;

	@Column(nullable = false)
	private int sortOrder;

	protected ArticleImage(String imageKey, int sortOrder) {
		this.imageKey = imageKey;
		this.sortOrder = sortOrder;
	}
}
