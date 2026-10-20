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
 * 수업 현장 게시물 (SCR-006). 공감·댓글을 받는다.
 */
@Entity
@Table(
	name = "post",
	indexes = @Index(name = "idx_post_category_created_at", columnList = "category, created_at")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends Article {

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "varchar(20)")
	private PostCategory category;

	// 목록 조회 때 집계 쿼리를 피하려고 비정규화한 값
	@Column(nullable = false)
	private int commentCount;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("sortOrder ASC")
	private List<PostImage> images = new ArrayList<>();

	@Builder
	private Post(Member member, PostCategory category, String title, String content) {
		super(member, title, content);
		this.category = category;
	}

	public void update(PostCategory category, String title, String content) {
		this.category = category;
		updateContent(title, content);
	}

	@Override
	public void addImage(String imageKey) {
		images.add(new PostImage(this, imageKey, images.size()));
	}

	@Override
	public void clearImages() {
		images.clear();
	}

	public void increaseCommentCount() {
		commentCount++;
	}

	public void decreaseCommentCount() {
		if (commentCount > 0) {
			commentCount--;
		}
	}
}
