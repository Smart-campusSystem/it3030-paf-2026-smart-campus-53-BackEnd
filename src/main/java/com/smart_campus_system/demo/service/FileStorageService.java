package com.smart_campus_system.demo.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.smart_campus_system.demo.config.FileStorageProperties;
import com.smart_campus_system.demo.exception.CustomException;
import com.smart_campus_system.demo.model.Ticket;
import com.smart_campus_system.demo.model.TicketAttachment;

@Service
public class FileStorageService {

	private static final int MAX_IMAGES = 3;

	private final FileStorageProperties properties;

	public FileStorageService(FileStorageProperties properties) {
		this.properties = properties;
	}

	public List<TicketAttachment> saveImages(Ticket ticket, List<MultipartFile> files) {
		if (files == null || files.isEmpty()) {
			return List.of();
		}
		if (files.size() > MAX_IMAGES) {
			throw new CustomException("At most " + MAX_IMAGES + " images are allowed per ticket");
		}
		List<TicketAttachment> saved = new ArrayList<>();
		Path dir = Path.of(properties.getDir()).toAbsolutePath().normalize();
		try {
			Files.createDirectories(dir);
		}
		catch (IOException e) {
			throw new CustomException("Could not create upload directory", e);
		}
		for (MultipartFile file : files) {
			if (file == null || file.isEmpty()) {
				continue;
			}
			String contentType = file.getContentType();
			if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
				throw new CustomException("Only image uploads are supported");
			}
			String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image";
			String ext = extension(original);
			String storedName = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
			Path target = dir.resolve(storedName);
			try (InputStream in = file.getInputStream()) {
				Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
			}
			catch (IOException e) {
				throw new CustomException("Failed to store file", e);
			}
			TicketAttachment att = new TicketAttachment();
			att.setTicket(ticket);
			att.setStoredPath("/api/files/" + storedName);
			att.setOriginalFilename(original);
			saved.add(att);
		}
		return saved;
	}

	private static String extension(String name) {
		int i = name.lastIndexOf('.');
		if (i < 0 || i == name.length() - 1) {
			return "";
		}
		return name.substring(i + 1).toLowerCase(Locale.ROOT);
	}
}
