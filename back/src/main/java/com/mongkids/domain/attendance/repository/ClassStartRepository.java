package com.mongkids.domain.attendance.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mongkids.domain.attendance.entity.ClassStart;
import com.mongkids.domain.attendance.entity.ClassStartStatus;

public interface ClassStartRepository extends JpaRepository<ClassStart, Long> {

	Optional<ClassStart> findByClassSessionId(Long classSessionId);

	boolean existsByClassSessionId(Long classSessionId);

	List<ClassStart> findByClassSessionIdIn(Collection<Long> classSessionIds);

	// 관리자 승인 대기 목록 (EMP-FR-011)
	@EntityGraph(attributePaths = {"classSession", "classSession.institution", "classSession.staff"})
	List<ClassStart> findByStatusOrderByStartedAtAsc(ClassStartStatus status);
}
