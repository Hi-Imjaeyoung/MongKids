package com.mongkids.domain.review.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mongkids.domain.review.entity.Review;
import com.mongkids.domain.review.entity.ReviewType;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	// 공개 목록은 숨김(FR-021)·삭제한 후기를 뺀다
	@EntityGraph(attributePaths = "member")
	Page<Review> findByDeletedAtIsNullAndHiddenAtIsNull(Pageable pageable);

	@EntityGraph(attributePaths = "member")
	Page<Review> findByTypeAndDeletedAtIsNullAndHiddenAtIsNull(ReviewType type, Pageable pageable);

	@EntityGraph(attributePaths = "member")
	Optional<Review> findByIdAndDeletedAtIsNull(Long id);
}
