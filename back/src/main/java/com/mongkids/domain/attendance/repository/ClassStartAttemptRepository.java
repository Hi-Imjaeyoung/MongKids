package com.mongkids.domain.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mongkids.domain.attendance.entity.ClassStartAttempt;

public interface ClassStartAttemptRepository extends JpaRepository<ClassStartAttempt, Long> {

	boolean existsByClassSessionId(Long classSessionId);
}
