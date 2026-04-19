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
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ProfileImageStorageTest {

	@Mock
	private S3Client s3Client;

	@Mock
	private S3Presigner s3Presigner;

	@Test
	void saveStoresCanonicalS3Uri() throws Exception {
		S3ProfileImageStorage storage = new S3ProfileImageStorage(
				s3Client, s3Presigner, "bucket-one", "profiles/", "https://cdn.example.com", 60);
		var file = new MockMultipartFile("file", "pic.png", "image/png", new byte[] {10, 11});

		String url = storage.save(3L, file);

		assertThat(url).startsWith("s3://bucket-one/profiles/3/");
		assertThat(url).endsWith(".png");
		ArgumentCaptor<PutObjectRequest> cap = ArgumentCaptor.forClass(PutObjectRequest.class);
		verify(s3Client).putObject(cap.capture(), any(RequestBody.class));
		assertThat(cap.getValue().bucket()).isEqualTo("bucket-one");
		assertThat(cap.getValue().contentType()).isEqualTo("image/png");
	}

	@Test
	void resolveDisplayUrlPresignsS3Uri() throws Exception {
		S3ProfileImageStorage storage = new S3ProfileImageStorage(
				s3Client, s3Presigner, "b", "profiles/", "", 60);
		PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);
		when(presigned.url()).thenReturn(URI.create("https://b.s3.ap-south-1.amazonaws.com/profiles/2/x.jpg?X-Amz-Signature=abc").toURL());
		when(s3Presigner.presignGetObject(isA(GetObjectPresignRequest.class))).thenReturn(presigned);

		String out = storage.resolveDisplayUrl("s3://b/profiles/2/x.jpg");

		assertThat(out).startsWith("https://");
		verify(s3Presigner).presignGetObject(isA(GetObjectPresignRequest.class));
	}
}
