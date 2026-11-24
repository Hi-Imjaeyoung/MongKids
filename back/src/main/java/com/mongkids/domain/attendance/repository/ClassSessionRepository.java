package com.mongkids.domain.attendance.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mongkids.domain.attendance.entity.ClassSession;

public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {

	@EntityGraph(attributePaths = {"institution", "staff"})
	Optional<ClassSession> findWithInstitutionById(Long id);

	// 관리자 기록 조회 (EMP-FR-003). 직원·기관이 null이면 전체(직원 미정 수업 포함), unassigned면 미정 수업만
	@EntityGraph(attributePaths = {"institution", "staff"})
	@Query("""
		select s from ClassSession s
		left join s.staff st
		where s.classDate between :from and :to
		  and (:staffId is null or st.id = :staffId)
		  and (:unassigned = false or s.staff is null)
		  and (:institutionId is null or s.institution.id = :institutionId)
		order by s.classDate, s.startTime, s.id
		""")
	List<ClassSession> searchForAdmin(@Param("from") LocalDate from, @Param("to") LocalDate to,
		@Param("staffId") Long staffId, @Param("unassigned") boolean unassigned,
		@Param("institutionId") Long institutionId);

	// 일괄 배정 (EMP-FR-021)
	@EntityGraph(attributePaths = {"institution", "staff"})
	List<ClassSession> findByIdIn(Collection<Long> ids);

	// 직원 본인 할당 조회 (EMP-FR-004), 수업 시간 겹침 확인
	@EntityGraph(attributePaths = "institution")
	List<ClassSession> findByStaffIdAndClassDateBetweenOrderByClassDateAscStartTimeAscIdAsc(Long staffId,
		LocalDate from, LocalDate to);

	long countByInstitutionId(Long institutionId);

	// 기관 삭제 (EMP-FR-012). 수업 시작·종료 기록과 시도 로그를 먼저 지운다
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from ClassSession s where s.institution.id = :institutionId")
	void deleteByInstitutionId(@Param("institutionId") Long institutionId);

	// 기관 목록에 보여 줄 기관별 수업 수
	@Query("""
		select s.institution.id as institutionId, count(s) as sessionCount
		from ClassSession s
		group by s.institution.id
		""")
	List<InstitutionSessionCount> countByInstitution();

	// 기관 표에 보여 줄 앞으로 수업 (EMP-SCR-006). 오늘은 끝 시각이 지나지 않은 수업만. institutionId가 null이면 전체
	@Query("""
		select s.institution.id as institutionId, s.classDate as classDate, s.startTime as startTime,
		       s.endTime as endTime, st.id as staffId
		from ClassSession s
		left join s.staff st
		where (s.classDate > :today or (s.classDate = :today and s.endTime > :now))
		  and (:institutionId is null or s.institution.id = :institutionId)
		order by s.classDate, s.startTime, s.id
		""")
	List<UpcomingSession> findUpcoming(@Param("today") LocalDate today, @Param("now") LocalTime now,
		@Param("institutionId") Long institutionId);

	interface UpcomingSession {

		Long getInstitutionId();

		LocalDate getClassDate();

		LocalTime getStartTime();

		LocalTime getEndTime();

		// null이면 직원 미정
		Long getStaffId();
	}

	interface InstitutionSessionCount {

		Long getInstitutionId();

		long getSessionCount();
	}
}
