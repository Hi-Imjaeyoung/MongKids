package com.mongkids.global.storage;

import java.io.InputStream;

/**
 * 저장소에서 읽은 파일. {@code content}는 받은 쪽이 닫는다.
 */
public record StoredFile(InputStream content, long size) {
}
