package com.mongkids.domain.attendance.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mongkids.domain.attendance.entity.DayCheckAttempt;

public interface DayCheckAttemptRepository extends JpaRepository<DayCheckAttempt, Long> {

	// 기관 삭제 (EMP-FR-012): 이 기관을 대조한 시도와, 함께 지우는 하루의 시도
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from DayCheckAttempt a where a.institution.id = :institutionId")
	void deleteByInstitutionId(@Param("institutionId") Long institutionId);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from DayCheckAttempt a where a.staff.id = :staffId and a.workDate = :workDate")
	void deleteByStaffIdAndWorkDate(@Param("staffId") Long staffId, @Param("workDate") LocalDate workDate);
}
