package com.mongkids.domain.post.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mongkids.domain.post.entity.LabCategory;
import com.mongkids.domain.post.entity.LabPost;

public interface LabPostRepository extends JpaRepository<LabPost, Long> {

	@EntityGraph(attributePaths = "member")
	Page<LabPost> findByDeletedAtIsNull(Pageable pageable);

	@EntityGraph(attributePaths = "member")
	Page<LabPost> findByCategoryAndDeletedAtIsNull(LabCategory category, Pageable pageable);

	@EntityGraph(attributePaths = "member")
	Page<LabPost> findByTitleContainingAndDeletedAtIsNull(String keyword, Pageable pageable);

	@EntityGraph(attributePaths = "member")
	Page<LabPost> findByCategoryAndTitleContainingAndDeletedAtIsNull(LabCategory category, String keyword,
		Pageable pageable);

	@EntityGraph(attributePaths = "member")
	Optional<LabPost> findByIdAndDeletedAtIsNull(Long id);
}
