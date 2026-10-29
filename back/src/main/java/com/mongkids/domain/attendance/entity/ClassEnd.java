package com.mongkids.domain.attendance.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.mongkids.domain.member.entity.Member;
import com.mongkids.global.common.BaseTimeEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 수업 종료 기록 (EMP-FR-010). 수업당 1건이고 위치는 쓰지 않는다.
 * 사진 묶음과 특이사항을 따로 저장하며, 작성자가 수업 당일까지 정정할 수 있다 (EMP-FR-014).
 */
@Entity
@Table(name = "class_end")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassEnd extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "class_session_id", nullable = false, unique = true)
	private ClassSession classSession;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "writer_id", nullable = false)
	private Member writer;

	@Column(nullable = false)
	private LocalDateTime endedAt;

	@Column(columnDefinition = "TEXT")
	private String note;

	// 마지막 정정 시각. 정정 이력은 보관하지 않는다
	private LocalDateTime correctedAt;

	@OneToMany(mappedBy = "classEnd", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("sortOrder ASC")
	private List<ClassEndImage> images = new ArrayList<>();

	public ClassEnd(ClassSession classSession, Member writer, String note, LocalDateTime endedAt) {
		this.classSession = classSession;
		this.writer = writer;
		this.note = note;
		this.endedAt = endedAt;
	}

	public void addImage(String imageKey) {
		images.add(new ClassEndImage(this, imageKey, images.size()));
	}

	/**
	 * 사진과 특이사항을 통째로 바꾼다. 수업 종료 시각은 그대로 둔다.
	 */
	public void correct(String note, List<String> imageKeys, LocalDateTime now) {
		this.note = note;
		images.clear();
		imageKeys.forEach(this::addImage);
		this.correctedAt = now;
	}

	public boolean isWrittenBy(Long memberId) {
		return writer.getId().equals(memberId);
	}
}
