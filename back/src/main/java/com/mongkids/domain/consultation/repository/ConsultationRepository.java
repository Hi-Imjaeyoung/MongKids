package com.mongkids.domain.consultation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mongkids.domain.consultation.entity.Consultation;
import com.mongkids.domain.consultation.entity.ConsultationStatus;
import com.mongkids.domain.consultation.entity.ConsultationType;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

	// 관리자 목록 (FR-004). 조건이 null이면 거르지 않는다
	@Query("""
		select c from Consultation c
		where (:type is null or c.type = :type)
		  and (:status is null or c.status = :status)
		""")
	Page<Consultation> search(@Param("type") ConsultationType type, @Param("status") ConsultationStatus status,
		Pageable pageable);

	// 신규 상담 건수 (FR-005). 인덱스 (status, created_at)의 앞부분만 읽는다
	long countByStatus(ConsultationStatus status);
}
