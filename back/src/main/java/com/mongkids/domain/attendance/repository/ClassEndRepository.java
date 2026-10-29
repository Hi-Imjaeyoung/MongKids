package com.mongkids.domain.attendance.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mongkids.domain.attendance.entity.ClassEnd;

public interface ClassEndRepository extends JpaRepository<ClassEnd, Long> {

	Optional<ClassEnd> findByClassSessionId(Long classSessionId);

	List<ClassEnd> findByClassSessionIdIn(Collection<Long> classSessionIds);
}
