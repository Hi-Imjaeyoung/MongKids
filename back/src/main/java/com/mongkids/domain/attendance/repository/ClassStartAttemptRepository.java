package com.mongkids.domain.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mongkids.domain.attendance.entity.ClassStartAttempt;

public interface ClassStartAttemptRepository extends JpaRepository<ClassStartAttempt, Long> {

	boolean existsByClassSessionId(Long classSessionId);

	// 기관 삭제 (EMP-FR-012)
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("""
		delete from ClassStartAttempt a
		where a.classSession.id in (select s.id from ClassSession s where s.institution.id = :institutionId)
		""")
	void deleteByInstitutionId(@Param("institutionId") Long institutionId);
}
