package com.mongkids.domain.review.repository;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mongkids.domain.review.entity.ReviewCode;

public interface ReviewCodeRepository extends JpaRepository<ReviewCode, Long> {

	boolean existsByCode(String code);

	// 관리자 목록 (FR-029). active가 null이면 전체, true면 사용 가능, false면 끝난 코드(폐기·다 씀·만료)
	@Query("""
		select c from ReviewCode c
		where :active is null
		   or (:active = true and c.revokedAt is null and c.usedCount < c.maxUses and c.expiresOn >= :today)
		   or (:active = false and (c.revokedAt is not null or c.usedCount >= c.maxUses or c.expiresOn < :today))
		""")
	Page<ReviewCode> searchForAdmin(@Param("active") Boolean active, @Param("today") LocalDate today,
		Pageable pageable);
}
