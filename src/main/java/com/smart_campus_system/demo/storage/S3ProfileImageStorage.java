package com.smart_campus_system.demo.storage;

import com.smart_campus_system.demo.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.profile-storage.mode", havingValue = "s3")
public class S3ProfileImageStorage implements ProfileImageStorage {

	private final S3Client s3Client;
	private final String bucket;
	private final String keyPrefix;
	private final String publicUrlPrefix;

	public S3ProfileImageStorage(
			S3Client s3Client,
			@Value("${app.profile-storage.s3.bucket}") String bucket,
			@Value("${app.profile-storage.s3.key-prefix:profiles/}") String keyPrefix,
			@Value("${app.profile-storage.s3.public-url-prefix:}") String publicUrlPrefix) {
		this.s3Client = s3Client;
		this.bucket = bucket;
		this.keyPrefix = keyPrefix.endsWith("/") ? keyPrefix : keyPrefix + "/";
		this.publicUrlPrefix = publicUrlPrefix.isBlank() ? "" : publicUrlPrefix.replaceAll("/$", "");
	}

	@Override
	public String save(Long userId, MultipartFile file) {
		String original = Objects.requireNonNullElse(file.getOriginalFilename(), "image");
		String ext = "";
		int dot = original.lastIndexOf('.');
		if (dot >= 0) {
			ext = original.substring(dot).toLowerCase();
			if (ext.length() > 8) {
				ext = "";
			}
		}
		String key = keyPrefix + userId + "/" + UUID.randomUUID() + ext;
		String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
		try {
			s3Client.putObject(
					PutObjectRequest.builder()
							.bucket(bucket)
							.key(key)
							.contentType(contentType)
							.build(),
					RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
		} catch (IOException e) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not upload to S3");
		}
		if (!publicUrlPrefix.isBlank()) {
			return publicUrlPrefix + "/" + key;
		}
		return "s3://" + bucket + "/" + key;
	}
}
