package com.mongkids.domain.attendance.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mongkids.domain.attendance.entity.ClassSeries;

public interface ClassSeriesRepository extends JpaRepository<ClassSeries, Long> {

	@EntityGraph(attributePaths = "institution")
	Optional<ClassSeries> findWithInstitutionById(Long id);

	@EntityGraph(attributePaths = "institution")
	List<ClassSeries> findByIdIn(Collection<Long> ids);

	// 기관 삭제 (EMP-FR-012). 수업 회차를 먼저 지운다
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from ClassSeries s where s.institution.id = :institutionId")
	void deleteByInstitutionId(@Param("institutionId") Long institutionId);
}
