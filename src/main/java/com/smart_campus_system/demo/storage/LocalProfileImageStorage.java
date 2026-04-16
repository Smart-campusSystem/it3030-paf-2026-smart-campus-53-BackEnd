package com.smart_campus_system.demo.storage;

import com.smart_campus_system.demo.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.profile-storage.mode", havingValue = "local", matchIfMissing = true)
public class LocalProfileImageStorage implements ProfileImageStorage {

	private final Path baseDir;
	private final String publicBaseUrl;

	public LocalProfileImageStorage(
			@Value("${app.profile-storage.local-dir:data/profile-uploads}") String localDir,
			@Value("${app.public-base-url:http://localhost:8080}") String publicBaseUrl) throws IOException {
		this.baseDir = Path.of(localDir).toAbsolutePath().normalize();
		Files.createDirectories(this.baseDir);
		this.publicBaseUrl = publicBaseUrl.replaceAll("/$", "");
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
		String filename = UUID.randomUUID() + ext;
		Path dir = baseDir.resolve(String.valueOf(userId));
		try {
			Files.createDirectories(dir);
			Path target = dir.resolve(filename);
			Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store profile image");
		}
		return publicBaseUrl + "/uploads/profile/" + userId + "/" + filename;
	}
}
