package com.mongkids.domain.attendance.entity;

import com.mongkids.global.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "class_end_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassEndImage extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "class_end_id", nullable = false)
	private ClassEnd classEnd;

	@Column(nullable = false, length = 255)
	private String imageKey;

	@Column(nullable = false)
	private int sortOrder;

	ClassEndImage(ClassEnd classEnd, String imageKey, int sortOrder) {
		this.classEnd = classEnd;
		this.imageKey = imageKey;
		this.sortOrder = sortOrder;
	}
}
