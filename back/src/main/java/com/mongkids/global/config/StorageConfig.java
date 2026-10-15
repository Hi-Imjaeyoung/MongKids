package com.mongkids.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mongkids.global.storage.FileStorage;
import com.mongkids.global.storage.S3FileStorage;
import com.mongkids.global.storage.StorageProperties;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * S3 파일 저장소 설정. 자격 증명은 AWS SDK 기본 체인(환경변수 {@code AWS_ACCESS_KEY_ID} 등, 운영은 IAM 역할)을 쓴다.
 */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {

	@Bean(destroyMethod = "close")
	public S3Client s3Client(StorageProperties properties) {
		return S3Client.builder()
			.region(Region.of(properties.region()))
			.build();
	}

	@Bean(destroyMethod = "close")
	public S3Presigner s3Presigner(StorageProperties properties) {
		return S3Presigner.builder()
			.region(Region.of(properties.region()))
			.build();
	}

	@Bean
	public FileStorage fileStorage(S3Client s3Client, S3Presigner s3Presigner, StorageProperties properties) {
		return new S3FileStorage(s3Client, s3Presigner, properties);
	}
}
