package com.mongkids.domain.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mongkids.domain.attendance.entity.DayEndNote;

// 기관별 특이사항. 저장·정정은 DayEnd에서 하고, 여기서는 기관 삭제(EMP-FR-012)에만 쓴다
public interface DayEndNoteRepository extends JpaRepository<DayEndNote, Long> {

	long countByInstitutionId(Long institutionId);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from DayEndNote n where n.institution.id = :institutionId")
	void deleteByInstitutionId(@Param("institutionId") Long institutionId);
}
