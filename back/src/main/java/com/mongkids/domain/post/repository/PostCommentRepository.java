package com.mongkids.domain.post.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mongkids.domain.post.entity.PostComment;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

	@EntityGraph(attributePaths = "member")
	Page<PostComment> findByPostIdAndDeletedAtIsNull(Long postId, Pageable pageable);

	@EntityGraph(attributePaths = "member")
	Optional<PostComment> findByIdAndPostIdAndDeletedAtIsNull(Long id, Long postId);
}
