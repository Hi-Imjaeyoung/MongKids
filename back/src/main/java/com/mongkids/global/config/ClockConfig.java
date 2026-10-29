package com.mongkids.global.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 서버 시각. "오늘"과 기록 시각을 테스트에서 고정할 수 있도록 주입해 쓴다 (근태 관리 EMP-FR-013, EMP-FR-014).
 */
@Configuration
public class ClockConfig {

	@Bean
	public Clock clock() {
		return Clock.system(ZoneId.of("Asia/Seoul"));
	}
}
