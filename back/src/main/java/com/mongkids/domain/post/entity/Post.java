package com.mongkids.domain.post.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.mongkids.domain.member.entity.Member;
import com.mongkids.global.common.BaseTimeEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "post",
	indexes = @Index(name = "idx_post_category_created_at", columnList = "category, created_at")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "varchar(20)")
	private PostCategory category;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	// 목록 조회 때 집계 쿼리를 피하려고 비정규화한 값
	@Column(nullable = false)
	private int likeCount;

	@Column(nullable = false)
	private int commentCount;

	private LocalDateTime deletedAt;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("sortOrder ASC")
	private List<PostImage> images = new ArrayList<>();

	@Builder
	private Post(Member member, PostCategory category, String title, String content) {
		this.member = member;
		this.category = category;
		this.title = title;
		this.content = content;
	}

	public void update(PostCategory category, String title, String content) {
		this.category = category;
		this.title = title;
		this.content = content;
	}

	public void addImage(String imageKey) {
		images.add(new PostImage(this, imageKey, images.size()));
	}

	public void clearImages() {
		images.clear();
	}

	public void increaseLikeCount() {
		likeCount++;
	}

	public void decreaseLikeCount() {
		if (likeCount > 0) {
			likeCount--;
		}
	}

	public void increaseCommentCount() {
		commentCount++;
	}

	public void decreaseCommentCount() {
		if (commentCount > 0) {
			commentCount--;
		}
	}

	public void delete() {
		this.deletedAt = LocalDateTime.now();
	}

	public boolean isDeleted() {
		return deletedAt != null;
	}
}
