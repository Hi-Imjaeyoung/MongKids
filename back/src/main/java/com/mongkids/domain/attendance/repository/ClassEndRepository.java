package com.mongkids.domain.attendance.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mongkids.domain.attendance.entity.ClassEnd;

public interface ClassEndRepository extends JpaRepository<ClassEnd, Long> {

	Optional<ClassEnd> findByClassSessionId(Long classSessionId);

	boolean existsByClassSessionId(Long classSessionId);

	List<ClassEnd> findByClassSessionIdIn(Collection<Long> classSessionIds);

	// 기관 삭제 (EMP-FR-012). 사진은 ClassEndImageRepository에서 먼저 지운다
	long countByClassSessionInstitutionId(Long institutionId);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("""
		delete from ClassEnd e
		where e.classSession.id in (select s.id from ClassSession s where s.institution.id = :institutionId)
		""")
	void deleteByInstitutionId(@Param("institutionId") Long institutionId);
}
