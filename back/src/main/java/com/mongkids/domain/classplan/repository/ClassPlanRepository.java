package com.mongkids.domain.classplan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mongkids.domain.classplan.entity.ClassPlan;

public interface ClassPlanRepository extends JpaRepository<ClassPlan, Long> {

	/** 연간(ANNUAL)이 먼저, 같은 종류는 올린 순서대로 */
	List<ClassPlan> findAllByOrderByKindAscIdAsc();
}
