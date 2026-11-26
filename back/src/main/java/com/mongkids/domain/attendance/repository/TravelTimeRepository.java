package com.mongkids.domain.attendance.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mongkids.domain.attendance.entity.TravelTime;

public interface TravelTimeRepository extends JpaRepository<TravelTime, Long> {

	// 고른 기관들 사이에 저장된 이동 시간
	@Query("""
		select t from TravelTime t
		where t.from.id in :institutionIds and t.to.id in :institutionIds
		""")
	List<TravelTime> findAmong(@Param("institutionIds") Collection<Long> institutionIds);

	// 기관 좌표 변경·삭제 (EMP-FR-023)
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from TravelTime t where t.from.id = :institutionId or t.to.id = :institutionId")
	void deleteByInstitutionId(@Param("institutionId") Long institutionId);
}
