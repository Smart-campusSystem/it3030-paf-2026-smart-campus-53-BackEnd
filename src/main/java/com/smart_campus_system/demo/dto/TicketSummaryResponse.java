package com.smart_campus_system.demo.dto;

import java.time.Instant;

import com.smart_campus_system.demo.model.Ticket;
import com.smart_campus_system.demo.model.TicketPriority;
import com.smart_campus_system.demo.model.TicketStatus;

public class TicketSummaryResponse {

	private Long id;
	private String category;
	/** Short title for lists: category plus first line of description. */
	private String subject;
	private String description;
	private TicketPriority priority;
	private TicketStatus status;
	private String contactName;
	private String contactEmail;
	private String contactPhone;
	private UserSummaryResponse submitter;
	private UserSummaryResponse assignedTechnician;
	private Instant createdAt;
	private Instant updatedAt;

	public static TicketSummaryResponse fromEntity(Ticket t) {
		TicketSummaryResponse r = new TicketSummaryResponse();
		r.id = t.getId();
		r.category = t.getCategory();
		r.subject = buildSubject(t);
		r.description = t.getDescription();
		r.priority = t.getPriority();
		r.status = t.getStatus();
		r.contactName = t.getContactName();
		r.contactEmail = t.getContactEmail();
		r.contactPhone = t.getContactPhone();
		r.submitter = UserSummaryResponse.fromEntity(t.getSubmitter());
		r.assignedTechnician = UserSummaryResponse.fromEntity(t.getAssignedTechnician());
		r.createdAt = t.getCreatedAt();
		r.updatedAt = t.getUpdatedAt() != null ? t.getUpdatedAt() : t.getCreatedAt();
		return r;
	}

	private static String buildSubject(Ticket t) {
		String cat = t.getCategory() != null ? t.getCategory().trim() : "Ticket";
		String desc = t.getDescription();
		if (desc == null || desc.isBlank()) {
			return cat;
		}
		String oneLine = desc.replace('\r', ' ').replace('\n', ' ').trim().replaceAll("\\s+", " ");
		if (oneLine.length() > 90) {
			return cat + " — " + oneLine.substring(0, 87) + "…";
		}
		return cat + " — " + oneLine;
	}

	public Long getId() {
		return id;
	}

	public String getCategory() {
		return category;
	}

	public String getSubject() {
		return subject;
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

	public UserSummaryResponse getSubmitter() {
		return submitter;
	}

	public UserSummaryResponse getAssignedTechnician() {
		return assignedTechnician;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
