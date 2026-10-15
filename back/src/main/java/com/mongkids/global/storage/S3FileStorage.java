package com.mongkids.global.storage;

import java.util.Optional;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

/**
 * S3 저장소. 업로드 URL에 Content-Type·Content-Length를 서명해 다른 타입·크기의 업로드는 S3가 거부한다.
 */
public class S3FileStorage implements FileStorage {

	private final S3Client s3Client;
	private final S3Presigner s3Presigner;
	private final StorageProperties properties;

	public S3FileStorage(S3Client s3Client, S3Presigner s3Presigner, StorageProperties properties) {
		this.s3Client = s3Client;
		this.s3Presigner = s3Presigner;
		this.properties = properties;
	}

	@Override
	public PresignedUpload createUploadUrl(String key, String contentType, long contentLength) {
		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			.bucket(properties.bucket())
			.key(key)
			.contentType(contentType)
			.contentLength(contentLength)
			.build();
		PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(request -> request
			.signatureDuration(properties.uploadUrlDuration())
			.putObjectRequest(putObjectRequest));
		return new PresignedUpload(presigned.url().toString(), presigned.expiration());
	}

	@Override
	public Optional<Long> findSize(String key) {
		try {
			Long size = s3Client.headObject(HeadObjectRequest.builder()
				.bucket(properties.bucket())
				.key(key)
				.build())
				.contentLength();
			return Optional.of(size);
		} catch (S3Exception e) {
			// HEAD 요청은 본문이 없어 404를 상태 코드로만 구분한다
			if (e.statusCode() == 404) {
				return Optional.empty();
			}
			throw e;
		}
	}

	@Override
	public String getUrl(String key) {
		String baseUrl = properties.publicBaseUrl();
		return baseUrl.endsWith("/") ? baseUrl + key : baseUrl + "/" + key;
	}
}
