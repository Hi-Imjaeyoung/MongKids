package com.mongkids.domain.review.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

	// 관리자 목록 (FR-021)은 숨긴 후기도 포함한다. 유형이 null이면 거르지 않는다
	@EntityGraph(attributePaths = "member")
	@Query("""
		select r from Review r
		where r.deletedAt is null
		  and (:type is null or r.type = :type)
		  and (:hiddenOnly = false or r.hiddenAt is not null)
		""")
	Page<Review> searchForAdmin(@Param("type") ReviewType type, @Param("hiddenOnly") boolean hiddenOnly,
		Pageable pageable);
}
