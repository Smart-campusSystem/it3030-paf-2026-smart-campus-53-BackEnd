package com.smart_campus_system.demo.dto;

import com.smart_campus_system.demo.model.TicketAttachment;

public class AttachmentResponse {

	private Long id;
	private String url;
	private String originalFilename;

	public static AttachmentResponse fromEntity(TicketAttachment a) {
		AttachmentResponse r = new AttachmentResponse();
		r.id = a.getId();
		r.url = a.getStoredPath();
		r.originalFilename = a.getOriginalFilename();
		return r;
	}

	public Long getId() {
		return id;
	}

	public String getUrl() {
		return url;
	}

	public String getOriginalFilename() {
		return originalFilename;
	}
}
