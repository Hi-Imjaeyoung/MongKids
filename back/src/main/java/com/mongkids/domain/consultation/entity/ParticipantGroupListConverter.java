package com.mongkids.domain.consultation.entity;

import jakarta.persistence.Converter;

@Converter
public class ParticipantGroupListConverter extends EnumListConverter<ParticipantGroup> {

	public ParticipantGroupListConverter() {
		super(ParticipantGroup.class);
	}
}
