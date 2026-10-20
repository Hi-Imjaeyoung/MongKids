package com.mongkids.domain.post.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.mongkids.domain.member.entity.Member;
import com.mongkids.global.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 직원이 쓰는 글의 공통 부모. 수업 현장 게시물({@link Post})과 몽키즈 연구소 글({@link LabPost})은 테이블을 따로 둔다.
 * 사진은 글마다 다른 테이블이라 하위 클래스가 관리한다.
 */
@MappedSuperclass
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Article extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	// 목록 조회 때 집계 쿼리를 피하려고 비정규화한 값
	@Column(nullable = false)
	private int likeCount;

	private LocalDateTime deletedAt;

	protected Article(Member member, String title, String content) {
		this.member = member;
		this.title = title;
		this.content = content;
	}

	public abstract List<? extends ArticleImage> getImages();

	public abstract void addImage(String imageKey);

	public abstract void clearImages();

	protected void updateContent(String title, String content) {
		this.title = title;
		this.content = content;
	}

	public void increaseLikeCount() {
		likeCount++;
	}

	public void decreaseLikeCount() {
		if (likeCount > 0) {
			likeCount--;
		}
	}

	public void delete() {
		this.deletedAt = LocalDateTime.now();
	}

	public boolean isDeleted() {
		return deletedAt != null;
	}
}
