package com.mongkids.global.storage;

import java.time.Instant;

/**
 * 업로드용 Presigned URL. 클라이언트는 발급 때 정한 Content-Type으로 이 URL에 PUT 한다.
 */
public record PresignedUpload(String uploadUrl, Instant expiresAt) {
}
