package com.smart_campus_system.demo.dto;

import java.time.Instant;

import com.smart_campus_system.demo.model.TicketComment;

public class CommentResponse {

	private Long id;
	private String text;
	private String authorUsername;
	private Long authorId;
	private Instant createdAt;
	private Instant updatedAt;

	public static CommentResponse fromEntity(TicketComment c) {
		CommentResponse r = new CommentResponse();
		r.id = c.getId();
		r.text = c.getBody();
		r.authorUsername = c.getAuthor().getUsername();
		r.authorId = c.getAuthor().getId();
		r.createdAt = c.getCreatedAt();
		r.updatedAt = c.getUpdatedAt();
		return r;
	}

	public Long getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public String getAuthorUsername() {
		return authorUsername;
	}

	public Long getAuthorId() {
		return authorId;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
