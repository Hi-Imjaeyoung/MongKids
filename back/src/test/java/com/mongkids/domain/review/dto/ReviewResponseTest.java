package com.mongkids.domain.review.dto;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ReviewResponseTest {

	@ParameterizedTest
	@CsvSource({
		"홍길동, 홍**",
		"김철, 김*",
		"남궁민수, 남***",
		"이, *",
		"Kim, K**",
		"' 박서준 ', 박**"
	})
	@DisplayName("작성자 이름은 첫 글자만 남기고 가린다 (FR-014)")
	void maskName(String name, String expected) {
		assertThat(ReviewResponse.maskName(name)).isEqualTo(expected);
	}
}
