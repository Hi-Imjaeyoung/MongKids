package com.mongkids.domain.attendance.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mongkids.domain.attendance.entity.ClassStartStatus;
import com.mongkids.domain.attendance.entity.DayEnd;

public interface DayEndRepository extends JpaRepository<DayEnd, Long> {

	@EntityGraph(attributePaths = "institution")
	Optional<DayEnd> findByStaffIdAndWorkDate(Long staffId, LocalDate workDate);

	@EntityGraph(attributePaths = "institution")
	List<DayEnd> findByStaffIdInAndWorkDateBetween(Collection<Long> staffIds, LocalDate from, LocalDate to);

	// 관리자 승인 대기 목록 (EMP-FR-020)
	List<DayEnd> findByStatus(ClassStartStatus status);

	// 기관 삭제 (EMP-FR-012): 이 기관을 대조한 하루 기록
	List<DayEnd> findByInstitutionId(Long institutionId);
}
