package com.mongkids.domain.consultation.entity;

import jakarta.persistence.Converter;

@Converter
public class QuoteProgramListConverter extends EnumListConverter<QuoteProgram> {

	public QuoteProgramListConverter() {
		super(QuoteProgram.class);
	}
}
