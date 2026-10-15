package com.mongkids.domain.consultation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mongkids.domain.consultation.entity.Consultation;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
}
