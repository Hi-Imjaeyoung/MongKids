package com.mongkids.domain.review.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
	name = "review",
	indexes = @Index(name = "idx_review_type_created_at", columnList = "type, created_at")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "varchar(20)")
	private ReviewType type;

	// 바나나 만족도 1~5
	@JdbcTypeCode(SqlTypes.TINYINT)
	@Column(nullable = false)
	private int satisfaction;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(columnDefinition = "TEXT")
	private String replyContent;

	private LocalDateTime repliedAt;

	private LocalDateTime hiddenAt;

	private LocalDateTime deletedAt;

	@OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("sortOrder ASC")
	private List<ReviewImage> images = new ArrayList<>();

	@Builder
	private Review(Member member, ReviewType type, int satisfaction, String content) {
		this.member = member;
		this.type = type;
		this.satisfaction = satisfaction;
		this.content = content;
	}

	public void update(ReviewType type, int satisfaction, String content) {
		this.type = type;
		this.satisfaction = satisfaction;
		this.content = content;
	}

	public void addImage(String imageKey) {
		images.add(new ReviewImage(this, imageKey, images.size()));
	}

	public void clearImages() {
		images.clear();
	}

	public void reply(String replyContent) {
		this.replyContent = replyContent;
		this.repliedAt = LocalDateTime.now();
	}

	public void hide() {
		this.hiddenAt = LocalDateTime.now();
	}

	public void unhide() {
		this.hiddenAt = null;
	}

	public boolean isHidden() {
		return hiddenAt != null;
	}

	public void delete() {
		this.deletedAt = LocalDateTime.now();
	}

	public boolean isDeleted() {
		return deletedAt != null;
	}
}
