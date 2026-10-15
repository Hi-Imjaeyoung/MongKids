package com.mongkids.global.storage;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 파일 저장소 설정 ({@code app.storage.*}).
 *
 * @param bucket            S3 버킷 이름
 * @param region            S3 리전 (예: ap-northeast-2)
 * @param publicBaseUrl     저장된 파일을 내려줄 주소 (CloudFront 도메인). 객체 키를 붙여 URL을 만든다
 * @param uploadUrlDuration 업로드용 Presigned URL 유효 기간
 */
@ConfigurationProperties(prefix = "app.storage")
public record StorageProperties(
	String bucket,
	String region,
	String publicBaseUrl,
	Duration uploadUrlDuration
) {
}
