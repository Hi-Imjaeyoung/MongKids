package com.mongkids.domain.post.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mongkids.domain.post.entity.LabCategory;
import com.mongkids.domain.post.entity.LabPost;

import jakarta.persistence.LockModeType;

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

	// 공감·취소 때 같은 글의 공감 처리를 한 번에 하나씩 하려고 행을 잠근다 (FR-011)
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select p from LabPost p where p.id = :id and p.deletedAt is null")
	Optional<LabPost> findForLikeById(@Param("id") Long id);
}
