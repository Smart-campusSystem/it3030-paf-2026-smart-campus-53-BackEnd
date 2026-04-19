package com.smart_campus_system.demo.dto;

import java.time.LocalDateTime;

import com.smart_campus_system.demo.model.Notification;

public class NotificationResponse {

	private Long id;
	private String message;
	private boolean read;
	private LocalDateTime createdAt;
	private Long ticketId;

	public static NotificationResponse fromEntity(Notification n) {
		NotificationResponse r = new NotificationResponse();
		r.id = n.getId();
		r.message = n.getMessage();
		r.read = n.isRead();
		r.createdAt = n.getCreatedAt();
		r.ticketId = n.getTicketId();
		return r;
	}

	public Long getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}

	public boolean isRead() {
		return read;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public Long getTicketId() {
		return ticketId;
	}
}
