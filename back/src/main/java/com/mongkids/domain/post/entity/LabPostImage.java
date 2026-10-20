package com.mongkids.domain.post.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lab_post_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LabPostImage extends ArticleImage {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "lab_post_id", nullable = false)
	private LabPost labPost;

	LabPostImage(LabPost labPost, String imageKey, int sortOrder) {
		super(imageKey, sortOrder);
		this.labPost = labPost;
	}
}
