package com.mongkids.domain.post.entity;

import java.util.ArrayList;
import java.util.List;

import com.mongkids.domain.member.entity.Member;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 몽키즈 연구소 글 (SCR-024). 공감만 받고 댓글은 없다.
 */
@Entity
@Table(
	name = "lab_post",
	indexes = @Index(name = "idx_lab_post_category_created_at", columnList = "category, created_at")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LabPost extends Article {

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "varchar(20)")
	private LabCategory category;

	@OneToMany(mappedBy = "labPost", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("sortOrder ASC")
	private List<LabPostImage> images = new ArrayList<>();

	@Builder
	private LabPost(Member member, LabCategory category, String title, String content) {
		super(member, title, content);
		this.category = category;
	}

	public void update(LabCategory category, String title, String content) {
		this.category = category;
		updateContent(title, content);
	}

	@Override
	public void addImage(String imageKey) {
		images.add(new LabPostImage(this, imageKey, images.size()));
	}

	@Override
	public void clearImages() {
		images.clear();
	}
}
