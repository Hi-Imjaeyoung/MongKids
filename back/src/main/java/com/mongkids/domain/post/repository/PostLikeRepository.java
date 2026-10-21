package com.mongkids.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mongkids.domain.post.entity.PostLike;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

	boolean existsByPostIdAndMemberId(Long postId, Long memberId);

	long deleteByPostIdAndMemberId(Long postId, Long memberId);
}
