package com.smart_campus_system.demo.storage;

import com.smart_campus_system.demo.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

@Component
public class S3ProfileImageStorage implements ProfileImageStorage {

	private final S3Client s3Client;
	private final S3Presigner s3Presigner;
	private final String bucket;
	private final String keyPrefix;
	private final String publicUrlPrefix;
	private final Duration presignDuration;

	public S3ProfileImageStorage(
			S3Client s3Client,
			S3Presigner s3Presigner,
			@Value("${app.profile-storage.s3.bucket}") String bucket,
			@Value("${app.profile-storage.s3.key-prefix:profiles/}") String keyPrefix,
			@Value("${app.profile-storage.s3.public-url-prefix:}") String publicUrlPrefix,
			@Value("${app.profile-storage.s3.presign-duration-minutes:60}") long presignDurationMinutes) {
		this.s3Client = s3Client;
		this.s3Presigner = s3Presigner;
		this.bucket = bucket;
		this.keyPrefix = keyPrefix.endsWith("/") ? keyPrefix : keyPrefix + "/";
		this.publicUrlPrefix = publicUrlPrefix.isBlank() ? "" : publicUrlPrefix.replaceAll("/$", "");
		this.presignDuration = Duration.ofMinutes(Math.max(5, presignDurationMinutes));
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
		String objectKey = keyPrefix + userId + "/" + UUID.randomUUID() + ext;
		String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
		try {
			s3Client.putObject(
					PutObjectRequest.builder()
							.bucket(bucket)
							.key(objectKey)
							.contentType(contentType)
							.build(),
					RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
		} catch (IOException e) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not upload to S3");
		}
		return "s3://" + bucket + "/" + objectKey;
	}

	@Override
	public String resolveDisplayUrl(String storedUrl) {
		if (storedUrl == null || storedUrl.isBlank()) {
			return null;
		}
		String trimmed = storedUrl.trim();
		ParsedObject ref = parseBucketAndKey(trimmed);
		if (ref == null) {
			return trimmed;
		}
		try {
			var getReq = GetObjectRequest.builder().bucket(ref.bucket()).key(ref.key()).build();
			var presignReq = GetObjectPresignRequest.builder()
					.signatureDuration(presignDuration)
					.getObjectRequest(getReq)
					.build();
			return s3Presigner.presignGetObject(presignReq).url().toExternalForm();
		} catch (RuntimeException e) {
			return trimmed;
		}
	}

	/**
	 * Resolves bucket + object key for presigning. Supports {@code s3://bucket/key}, HTTPS virtual-hosted
	 * S3 URLs, and HTTPS URLs under {@code public-url-prefix} (e.g. CloudFront).
	 */
	private ParsedObject parseBucketAndKey(String stored) {
		if (stored.startsWith("s3://")) {
			String rest = stored.substring(5);
			int slash = rest.indexOf('/');
			if (slash <= 0 || slash >= rest.length() - 1) {
				return null;
			}
			return new ParsedObject(rest.substring(0, slash), rest.substring(slash + 1));
		}
		if (stored.startsWith("https://") || stored.startsWith("http://")) {
			if (!publicUrlPrefix.isBlank() && stored.startsWith(publicUrlPrefix)) {
				String suffix = stored.substring(publicUrlPrefix.length());
				if (suffix.startsWith("/")) {
					suffix = suffix.substring(1);
				}
				int q = suffix.indexOf('?');
				if (q >= 0) {
					suffix = suffix.substring(0, q);
				}
				if (!suffix.isBlank()) {
					return new ParsedObject(bucket, suffix);
				}
			}
			try {
				URI u = URI.create(stored);
				String host = u.getHost();
				String path = u.getPath();
				if (host == null || path == null || path.isBlank()) {
					return null;
				}
				if (path.startsWith("/")) {
					path = path.substring(1);
				}
				if (host.contains(".s3.") && host.endsWith(".amazonaws.com")) {
					String bucketFromHost = host.substring(0, host.indexOf(".s3"));
					return new ParsedObject(bucketFromHost, path);
				}
			} catch (IllegalArgumentException ignored) {
				return null;
			}
		}
		return null;
	}

	private record ParsedObject(String bucket, String key) {}
}
