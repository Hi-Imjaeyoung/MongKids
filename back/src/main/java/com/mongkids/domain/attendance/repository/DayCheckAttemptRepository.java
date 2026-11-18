package com.mongkids.domain.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mongkids.domain.attendance.entity.DayCheckAttempt;

public interface DayCheckAttemptRepository extends JpaRepository<DayCheckAttempt, Long> {
}
