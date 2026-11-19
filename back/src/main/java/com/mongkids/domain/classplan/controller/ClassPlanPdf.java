package com.mongkids.domain.classplan.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.mongkids.global.storage.StoredFile;

/**
 * 계획안 PDF 응답. 화면 안에서만 보므로 inline으로 내보내고 저장 이름을 주지 않는다 (SCR-027).
 */
final class ClassPlanPdf {

	private ClassPlanPdf() {
	}

	static ResponseEntity<InputStreamResource> of(StoredFile file, CacheControl cacheControl) {
		return ResponseEntity.ok()
			.contentType(MediaType.APPLICATION_PDF)
			.contentLength(file.size())
			.cacheControl(cacheControl)
			.header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().build().toString())
			.body(new InputStreamResource(file.content()));
	}
}
