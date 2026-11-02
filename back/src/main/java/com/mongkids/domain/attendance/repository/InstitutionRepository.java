package com.mongkids.domain.attendance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mongkids.domain.attendance.entity.Institution;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {

	List<Institution> findAllByOrderByNameAscIdAsc();
}
