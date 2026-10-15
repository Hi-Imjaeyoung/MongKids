package com.mongkids.domain.post.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mongkids.domain.post.entity.Post;
import com.mongkids.domain.post.entity.PostCategory;

public interface PostRepository extends JpaRepository<Post, Long> {

	@EntityGraph(attributePaths = "member")
	Page<Post> findByDeletedAtIsNull(Pageable pageable);

	@EntityGraph(attributePaths = "member")
	Page<Post> findByCategoryAndDeletedAtIsNull(PostCategory category, Pageable pageable);

	@EntityGraph(attributePaths = "member")
	Optional<Post> findByIdAndDeletedAtIsNull(Long id);
}
