package com.mongkids.domain.post.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mongkids.domain.post.entity.Post;
import com.mongkids.domain.post.entity.PostCategory;

public interface PostRepository extends JpaRepository<Post, Long> {

	@EntityGraph(attributePaths = "member")
	Page<Post> findByDeletedAtIsNull(Pageable pageable);

	@EntityGraph(attributePaths = "member")
	Page<Post> findByCategoryAndDeletedAtIsNull(PostCategory category, Pageable pageable);

	@EntityGraph(attributePaths = "member")
	Optional<Post> findByIdAndDeletedAtIsNull(Long id);

	// 댓글 수는 동시에 달린 댓글이 서로 덮어쓰지 않도록 DB에서 바로 더하고 뺀다 (FR-010)
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("update Post p set p.commentCount = p.commentCount + 1 where p.id = :postId")
	void increaseCommentCount(@Param("postId") Long postId);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("update Post p set p.commentCount = p.commentCount - 1 where p.id = :postId and p.commentCount > 0")
	void decreaseCommentCount(@Param("postId") Long postId);
}
