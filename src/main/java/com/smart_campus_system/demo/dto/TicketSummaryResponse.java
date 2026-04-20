package com.smart_campus_system.demo.dto;

import java.time.Instant;

import com.smart_campus_system.demo.model.Ticket;
import com.smart_campus_system.demo.model.TicketPriority;
import com.smart_campus_system.demo.model.TicketStatus;

public class TicketSummaryResponse {

	private Long id;
	private String category;
	private String description;
	private TicketPriority priority;
	private TicketStatus status;
	private String contactName;
	private UserSummaryResponse assignedTechnician;
	private Instant createdAt;

	public static TicketSummaryResponse fromEntity(Ticket t) {
		TicketSummaryResponse r = new TicketSummaryResponse();
		r.id = t.getId();
		r.category = t.getCategory();
		r.description = t.getDescription();
		r.priority = t.getPriority();
		r.status = t.getStatus();
		r.contactName = t.getContactName();
		r.assignedTechnician = UserSummaryResponse.fromEntity(t.getAssignedTechnician());
		r.createdAt = t.getCreatedAt();
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

	public UserSummaryResponse getAssignedTechnician() {
		return assignedTechnician;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
