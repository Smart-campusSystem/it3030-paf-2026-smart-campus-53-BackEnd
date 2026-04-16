package com.smart_campus_system.demo.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ProfileImageStorage {

	String save(Long userId, MultipartFile file);
}
