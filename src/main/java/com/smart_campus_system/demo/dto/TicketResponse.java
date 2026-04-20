package com.smart_campus_system.demo.dto;

import java.time.Instant;
import java.util.List;

import com.smart_campus_system.demo.model.Ticket;
import com.smart_campus_system.demo.model.TicketPriority;
import com.smart_campus_system.demo.model.TicketStatus;

public class TicketResponse {

	private Long id;
	private String category;
	private String description;
	private TicketPriority priority;
	private TicketStatus status;
	private String contactName;
	private String contactEmail;
	private String contactPhone;
	private UserSummaryResponse assignedTechnician;
	/** Present when the ticket was filed by a signed-in user (null for anonymous-only rows). */
	private UserSummaryResponse submitter;
	private Instant createdAt;
	private List<AttachmentResponse> attachments;
	private List<CommentResponse> comments;

	public static TicketResponse fromEntity(Ticket t) {
		TicketResponse r = new TicketResponse();
		r.id = t.getId();
		r.category = t.getCategory();
		r.description = t.getDescription();
		r.priority = t.getPriority();
		r.status = t.getStatus();
		r.contactName = t.getContactName();
		r.contactEmail = t.getContactEmail();
		r.contactPhone = t.getContactPhone();
		r.assignedTechnician = UserSummaryResponse.fromEntity(t.getAssignedTechnician());
		r.submitter = UserSummaryResponse.fromEntity(t.getSubmitter());
		r.createdAt = t.getCreatedAt();
		r.attachments = t.getAttachments().stream().map(AttachmentResponse::fromEntity).toList();
		r.comments = t.getComments().stream().map(CommentResponse::fromEntity).toList();
		return r;
	}

	public Long getId() {
		return id;
	}

	public String getCategory() {
		return category;
	}

	public String getDescription() {
		return description;
	}

	public TicketPriority getPriority() {
		return priority;
	}

	public TicketStatus getStatus() {
		return status;
	}

	public String getContactName() {
		return contactName;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public String getContactPhone() {
		return contactPhone;
	}

	public UserSummaryResponse getAssignedTechnician() {
		return assignedTechnician;
	}

	public UserSummaryResponse getSubmitter() {
		return submitter;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public List<AttachmentResponse> getAttachments() {
		return attachments;
	}

	public List<CommentResponse> getComments() {
		return comments;
	}
}
