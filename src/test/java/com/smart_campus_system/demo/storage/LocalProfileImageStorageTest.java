package com.smart_campus_system.demo.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LocalProfileImageStorageTest {

	@Test
	void savesFileAndReturnsPublicUrl(@TempDir Path tempDir) throws Exception {
		LocalProfileImageStorage storage = new LocalProfileImageStorage(
				tempDir.resolve("uploads").toString(),
				"http://api.test");

		var file = new MockMultipartFile("file", "photo.png", "image/png", new byte[] {1, 2, 3, 4});
		String url = storage.save(7L, file);

		assertThat(url).startsWith("http://api.test/uploads/profile/7/");
		assertThat(url).endsWith(".png");
	}
}
