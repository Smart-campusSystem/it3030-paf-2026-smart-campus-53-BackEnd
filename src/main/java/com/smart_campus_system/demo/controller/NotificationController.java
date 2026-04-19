package com.smart_campus_system.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.smart_campus_system.demo.dto.AdminNotificationRequest;
import com.smart_campus_system.demo.dto.NotificationResponse;
import com.smart_campus_system.demo.exception.NotFoundException;
import com.smart_campus_system.demo.model.Notification;
import com.smart_campus_system.demo.repository.NotificationRepository;
import com.smart_campus_system.demo.repository.UserRepository;
import com.smart_campus_system.demo.service.NotificationService;
import com.smart_campus_system.demo.service.SseService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationRepository notificationRepository;
	private final SseService sseService;
	private final NotificationService notificationService;
	private final com.smart_campus_system.demo.repository.UserRepository userRepository;

	public NotificationController(
			NotificationRepository notificationRepository,
			SseService sseService,
			NotificationService notificationService,
			com.smart_campus_system.demo.repository.UserRepository userRepository) {
		this.notificationRepository = notificationRepository;
		this.sseService = sseService;
		this.notificationService = notificationService;
		this.userRepository = userRepository;
	}

	// ── SSE subscribe ─────────────────────────────────────────────────────────
	@GetMapping("/subscribe")
	public SseEmitter subscribe(Authentication authentication) {
		String email = resolveEmail(authentication);
		return sseService.subscribe(email);
	}

	// ── Current user: fetch own notifications ─────────────────────────────────
	@GetMapping
	public List<NotificationResponse> mine(Authentication authentication) {
		String email = resolveEmail(authentication);
		return notificationRepository.findByUserEmailOrderByCreatedAtDesc(email).stream()
				.map(NotificationResponse::fromEntity)
				.toList();
	}

	// ── Current user: unread count ────────────────────────────────────────────
	@GetMapping("/unread-count")
	public long unreadCount(Authentication authentication) {
		return notificationRepository.countByUserEmailAndIsReadFalse(resolveEmail(authentication));
	}

	// ── Current user: mark single notification as read ────────────────────────
	@PatchMapping("/{id}/read")
	public NotificationResponse markRead(@PathVariable Long id, Authentication authentication) {
		Notification n = notificationRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Notification not found"));
		if (!n.getUserEmail().equalsIgnoreCase(resolveEmail(authentication))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		n.setRead(true);
		return NotificationResponse.fromEntity(notificationRepository.save(n));
	}

	/** Extracts email from either UserPrincipal (JWT) or plain string principal (OAuth2 session). */
	private String resolveEmail(Authentication authentication) {
		if (authentication == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		Object principal = authentication.getPrincipal();
		if (principal instanceof com.smart_campus_system.demo.security.UserPrincipal up) {
			return up.email();
		}
		// OAuth2 session or plain string principal
		String name = authentication.getName();
		if (name == null || name.isBlank()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		return name;
	}


	// ── ADMIN: send notification to one user or broadcast ─────────────────────
	@PostMapping("/admin/send")
	@PreAuthorize("hasRole('ADMIN')")
	public NotificationResponse adminSend(
			@RequestBody AdminNotificationRequest req,
			Authentication authentication) {

		String adminEmail = authentication.getName();
		String type = req.getType() != null ? req.getType() : "INFO";

		if (req.getUserEmail() != null && !req.getUserEmail().isBlank()) {
			// Send to specific user
			Notification n = Notification.builder()
					.message(req.getMessage())
					.type(type)
					.userEmail(req.getUserEmail().toLowerCase().trim())
					.isRead(false)
					.createdAt(java.time.LocalDateTime.now())
					.createdByEmail(adminEmail)
					.build();
			Notification saved = notificationRepository.save(n);
			sseService.send(saved.getUserEmail(), saved);
			return NotificationResponse.fromEntity(saved);
		} else {
			// Broadcast to ALL users — send to the first user as the representative response
			List<com.smart_campus_system.demo.model.User> allUsers = userRepository.findAll();
			Notification last = null;
			for (var user : allUsers) {
				Notification n = Notification.builder()
						.message(req.getMessage())
						.type(type)
						.userEmail(user.getEmail())
						.isRead(false)
						.createdAt(java.time.LocalDateTime.now())
						.createdByEmail(adminEmail)
						.build();
				last = notificationRepository.save(n);
				sseService.send(user.getEmail(), last);
			}
			return last != null ? NotificationResponse.fromEntity(last) : null;
		}
	}

	// ── ADMIN: view all notifications this admin created ──────────────────────
	@GetMapping("/admin/sent")
	@PreAuthorize("hasRole('ADMIN')")
	public List<NotificationResponse> adminSent(Authentication authentication) {
		return notificationRepository.findByCreatedByEmailOrderByCreatedAtDesc(resolveEmail(authentication))
				.stream().map(NotificationResponse::fromEntity).toList();
	}

	// ── ADMIN: update a notification they created ─────────────────────────────
	@PutMapping("/admin/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public NotificationResponse adminUpdate(
			@PathVariable Long id,
			@RequestBody AdminNotificationRequest req,
			Authentication authentication) {
		Notification n = notificationRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Notification not found"));
		if (!authentication.getName().equalsIgnoreCase(n.getCreatedByEmail())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only edit notifications you created");
		}
		if (req.getMessage() != null) n.setMessage(req.getMessage());
		if (req.getType()    != null) n.setType(req.getType());
		return NotificationResponse.fromEntity(notificationRepository.save(n));
	}

	// ── ADMIN: delete a notification they created ─────────────────────────────
	@DeleteMapping("/admin/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void adminDelete(@PathVariable Long id, Authentication authentication) {
		Notification n = notificationRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Notification not found"));
		if (!authentication.getName().equalsIgnoreCase(n.getCreatedByEmail())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete notifications you created");
		}
		notificationRepository.delete(n);
	}
}
