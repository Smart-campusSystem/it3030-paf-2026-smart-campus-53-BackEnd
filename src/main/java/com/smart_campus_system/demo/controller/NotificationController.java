package com.smart_campus_system.demo.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.smart_campus_system.demo.dto.NotificationResponse;
import com.smart_campus_system.demo.exception.NotFoundException;
import com.smart_campus_system.demo.repository.NotificationRepository;
import com.smart_campus_system.demo.repository.UserRepository;
import com.smart_campus_system.demo.service.SseService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationRepository notificationRepository;
	private final UserRepository userRepository;
	private final SseService sseService;

	public NotificationController(NotificationRepository notificationRepository, UserRepository userRepository, SseService sseService) {
		this.notificationRepository = notificationRepository;
		this.userRepository = userRepository;
		this.sseService = sseService;
	}

	@GetMapping("/subscribe")
	public SseEmitter subscribe(Authentication authentication) {
		// Uses Authentication to derive context consistently across the project
		var user = userRepository.findByEmail(authentication.getName())
				.orElseThrow(() -> new NotFoundException("User not found"));
		return sseService.subscribe(user.getEmail());
	}

	@GetMapping
	public List<NotificationResponse> mine(Authentication authentication) {
		var user = userRepository.findByEmail(authentication.getName())
				.orElseThrow(() -> new NotFoundException("User not found"));
		return notificationRepository.findByUserEmailOrderByCreatedAtDesc(user.getEmail()).stream()
				.map(NotificationResponse::fromEntity)
				.toList();
	}
}
