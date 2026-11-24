package com.mongkids.global.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

/**
 * S3 저장소. 업로드 URL에 Content-Type·Content-Length를 서명해 다른 타입·크기의 업로드는 S3가 거부한다.
 */
public class S3FileStorage implements FileStorage {

	// DeleteObjects 한 번에 지울 수 있는 최대 키 수
	private static final int DELETE_BATCH_SIZE = 1000;

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

	@Override
	public Optional<StoredFile> open(String key) {
		try {
			ResponseInputStream<GetObjectResponse> object = s3Client.getObject(GetObjectRequest.builder()
				.bucket(properties.bucket())
				.key(key)
				.build());
			return Optional.of(new StoredFile(object, object.response().contentLength()));
		} catch (NoSuchKeyException e) {
			return Optional.empty();
		}
	}

	@Override
	public List<String> delete(Collection<String> keys) {
		List<String> all = List.copyOf(keys);
		List<String> failed = new ArrayList<>();
		for (int from = 0; from < all.size(); from += DELETE_BATCH_SIZE) {
			List<String> batch = all.subList(from, Math.min(from + DELETE_BATCH_SIZE, all.size()));
			try {
				DeleteObjectsResponse response = s3Client.deleteObjects(DeleteObjectsRequest.builder()
					.bucket(properties.bucket())
					.delete(Delete.builder()
						.objects(batch.stream().map(key -> ObjectIdentifier.builder().key(key).build()).toList())
						.quiet(true)
						.build())
					.build());
				response.errors().forEach(error -> failed.add(error.key()));
			} catch (S3Exception e) {
				failed.addAll(batch);
			}
		}
		return failed;
	}
}
