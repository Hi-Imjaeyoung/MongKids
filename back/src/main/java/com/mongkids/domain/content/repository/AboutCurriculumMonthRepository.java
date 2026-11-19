package com.mongkids.domain.content.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mongkids.domain.content.entity.AboutCurriculumMonth;

public interface AboutCurriculumMonthRepository extends JpaRepository<AboutCurriculumMonth, Long> {

	List<AboutCurriculumMonth> findAllByOrderBySortOrderAsc();
}
