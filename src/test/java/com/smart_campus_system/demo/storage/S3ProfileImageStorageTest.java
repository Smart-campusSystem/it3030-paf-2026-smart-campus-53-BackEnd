package com.smart_campus_system.demo.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class S3ProfileImageStorageTest {

	@Mock
	private S3Client s3Client;

	@Test
	void uploadsWithConfiguredBucketAndPublicPrefix() throws Exception {
		S3ProfileImageStorage storage = new S3ProfileImageStorage(s3Client, "bucket-one", "profiles/", "https://cdn.example.com");
		var file = new MockMultipartFile("file", "pic.png", "image/png", new byte[] {10, 11});

		String url = storage.save(3L, file);

		assertThat(url).startsWith("https://cdn.example.com/profiles/3/");
		ArgumentCaptor<PutObjectRequest> cap = ArgumentCaptor.forClass(PutObjectRequest.class);
		verify(s3Client).putObject(cap.capture(), any(RequestBody.class));
		assertThat(cap.getValue().bucket()).isEqualTo("bucket-one");
		assertThat(cap.getValue().contentType()).isEqualTo("image/png");
	}

	@Test
	void returnsS3UriWhenNoPublicPrefix() throws Exception {
		S3ProfileImageStorage storage = new S3ProfileImageStorage(s3Client, "b", "", "");
		var file = new MockMultipartFile("file", "x.bin", null, new byte[] {1});

		String url = storage.save(2L, file);
		verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
		assertThat(url).startsWith("s3://b/");
	}
}
