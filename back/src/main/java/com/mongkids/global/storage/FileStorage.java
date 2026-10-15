package com.mongkids.global.storage;

import java.util.Optional;

/**
 * 파일 저장소. 파일은 클라이언트가 Presigned URL로 직접 올리고, 서버는 URL 발급과 확인만 한다.
 */
public interface FileStorage {

	/**
	 * 지정한 키·타입·크기로만 올릴 수 있는 업로드 URL을 만든다.
	 */
	PresignedUpload createUploadUrl(String key, String contentType, long contentLength);

	/**
	 * 저장된 파일의 크기. 파일이 없으면 빈 값.
	 */
	Optional<Long> findSize(String key);

	/**
	 * 파일을 내려받을 공개 URL.
	 */
	String getUrl(String key);
}
