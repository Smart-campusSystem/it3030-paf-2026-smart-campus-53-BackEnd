package com.smart_campus_system.demo.dto;

import java.time.LocalDateTime;

import com.smart_campus_system.demo.model.Notification;

public class NotificationResponse {

	private Long id;
	private String message;
	private String type;
	private boolean read;
	private LocalDateTime createdAt;
	private Long ticketId;
	private String userEmail;
	private String createdByEmail;

	public static NotificationResponse fromEntity(Notification n) {
		NotificationResponse r = new NotificationResponse();
		r.id = n.getId();
		r.message = n.getMessage();
		r.type = n.getType();
		r.read = n.isRead();
		r.createdAt = n.getCreatedAt();
		r.ticketId = n.getTicketId();
		r.userEmail = n.getUserEmail();
		r.createdByEmail = n.getCreatedByEmail();
		return r;
	}

	public Long getId()              { return id; }
	public String getMessage()       { return message; }
	public String getType()          { return type; }
	public boolean isRead()          { return read; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public Long getTicketId()        { return ticketId; }
	public String getUserEmail()     { return userEmail; }
	public String getCreatedByEmail() { return createdByEmail; }
}
