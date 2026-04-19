package com.smart_campus_system.demo.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smart_campus_system.demo.dto.NotificationResponse;
import com.smart_campus_system.demo.exception.NotFoundException;
import com.smart_campus_system.demo.repository.NotificationRepository;
import com.smart_campus_system.demo.repository.UserRepository;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationRepository notificationRepository;
	private final UserRepository userRepository;

	public NotificationController(NotificationRepository notificationRepository, UserRepository userRepository) {
		this.notificationRepository = notificationRepository;
		this.userRepository = userRepository;
	}

	@GetMapping
	public List<NotificationResponse> mine(Authentication authentication) {
		var user = userRepository.findByEmail(authentication.getName())
				.orElseThrow(() -> new NotFoundException("User not found"));
		return notificationRepository.findByUserOrderByCreatedAtDesc(user).stream()
				.map(NotificationResponse::fromEntity)
				.toList();
	}
}
