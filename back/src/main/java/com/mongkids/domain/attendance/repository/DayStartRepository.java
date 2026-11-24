package com.mongkids.domain.attendance.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mongkids.domain.attendance.entity.ClassStartStatus;
import com.mongkids.domain.attendance.entity.DayStart;

public interface DayStartRepository extends JpaRepository<DayStart, Long> {

	@EntityGraph(attributePaths = "institution")
	Optional<DayStart> findByStaffIdAndWorkDate(Long staffId, LocalDate workDate);

	// 하루 기록이 있는 날의 수업 할당 잠금 (EMP-FR-004)
	boolean existsByStaffIdAndWorkDate(Long staffId, LocalDate workDate);

	@Query("""
		select count(d) > 0 from DayStart d
		where d.staff.id = :staffId and d.workDate in :dates
		""")
	boolean existsByStaffIdAndWorkDateIn(@Param("staffId") Long staffId, @Param("dates") Collection<LocalDate> dates);

	@EntityGraph(attributePaths = "institution")
	List<DayStart> findByStaffIdInAndWorkDateBetween(Collection<Long> staffIds, LocalDate from, LocalDate to);

	// 관리자 승인 대기 목록 (EMP-FR-020)
	List<DayStart> findByStatus(ClassStartStatus status);

	// 기관 삭제 (EMP-FR-012): 이 기관을 대조한 하루 기록
	List<DayStart> findByInstitutionId(Long institutionId);
}
