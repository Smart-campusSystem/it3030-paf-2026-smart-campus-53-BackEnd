package com.smart_campus_system.demo.dto;

import java.time.Instant;

import com.smart_campus_system.demo.model.Notification;

public class NotificationResponse {

	private Long id;
	private String message;
	private boolean read;
	private Instant createdAt;
	private Long ticketId;

	public static NotificationResponse fromEntity(Notification n) {
		NotificationResponse r = new NotificationResponse();
		r.id = n.getId();
		r.message = n.getMessage();
		r.read = n.isReadFlag();
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

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Long getTicketId() {
		return ticketId;
	}
}
