package com.mongkids.domain.attendance.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mongkids.domain.attendance.entity.ClassSession;
import com.mongkids.domain.attendance.entity.ClassStartStatus;

public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {

	@EntityGraph(attributePaths = {"institution", "staff"})
	Optional<ClassSession> findWithInstitutionById(Long id);

	// 오늘 수업과, 수업 시작이 반려된 채 남은 지난 수업 (EMP-FR-013)
	@EntityGraph(attributePaths = "institution")
	@Query("""
		select s from ClassSession s
		where s.staff.id = :staffId
		  and (s.classDate = :today
		    or (s.classDate < :today and exists (
		      select 1 from ClassStart c where c.classSession = s and c.status = :rejected)))
		order by s.classDate, s.startTime, s.id
		""")
	List<ClassSession> findTodayClasses(@Param("staffId") Long staffId, @Param("today") LocalDate today,
		@Param("rejected") ClassStartStatus rejected);

	// 관리자 기록 조회 (EMP-FR-003). 직원이 null이면 전 직원
	@EntityGraph(attributePaths = {"institution", "staff"})
	@Query("""
		select s from ClassSession s
		where s.classDate between :from and :to
		  and (:staffId is null or s.staff.id = :staffId)
		order by s.classDate, s.startTime, s.id
		""")
	List<ClassSession> searchForAdmin(@Param("from") LocalDate from, @Param("to") LocalDate to,
		@Param("staffId") Long staffId);
}
