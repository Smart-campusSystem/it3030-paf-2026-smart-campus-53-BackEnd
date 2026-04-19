package com.smart_campus_system.demo.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ProfileImageStorage {

	String save(Long userId, MultipartFile file);

	/**
	 * URL safe for embedding in HTML (e.g. {@code <img src>}). For S3, returns a time-limited presigned GET URL
	 * so private buckets work; otherwise returns the stored value.
	 */
	default String resolveDisplayUrl(String storedUrl) {
		return storedUrl;
	}
}
