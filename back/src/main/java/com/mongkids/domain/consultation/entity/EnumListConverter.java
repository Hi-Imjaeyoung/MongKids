package com.mongkids.domain.consultation.entity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.AttributeConverter;

/**
 * 여러 개 고른 선택지를 enum 이름을 쉼표로 이은 문자열 한 컬럼에 저장한다 ({@code PRESCHOOL,ELEMENTARY}).
 * 고른 것이 없으면 NULL로 둔다. 조회 조건으로 쓰지 않는 값이라 별도 테이블로 나누지 않는다.
 */
public abstract class EnumListConverter<E extends Enum<E>> implements AttributeConverter<List<E>, String> {

	private static final String SEPARATOR = ",";

	private final Class<E> type;

	protected EnumListConverter(Class<E> type) {
		this.type = type;
	}

	@Override
	public String convertToDatabaseColumn(List<E> values) {
		if (values == null || values.isEmpty()) {
			return null;
		}
		return values.stream().map(Enum::name).collect(Collectors.joining(SEPARATOR));
	}

	@Override
	public List<E> convertToEntityAttribute(String column) {
		if (column == null || column.isBlank()) {
			return List.of();
		}
		return Arrays.stream(column.split(SEPARATOR)).map(name -> Enum.valueOf(type, name)).toList();
	}
}
