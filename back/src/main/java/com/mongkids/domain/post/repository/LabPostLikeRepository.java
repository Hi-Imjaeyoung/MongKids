package com.mongkids.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mongkids.domain.post.entity.LabPostLike;

public interface LabPostLikeRepository extends JpaRepository<LabPostLike, Long> {

	boolean existsByLabPostIdAndMemberId(Long labPostId, Long memberId);

	long deleteByLabPostIdAndMemberId(Long labPostId, Long memberId);
}
