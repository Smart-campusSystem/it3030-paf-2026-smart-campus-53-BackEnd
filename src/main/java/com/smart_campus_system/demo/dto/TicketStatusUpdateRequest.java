package com.smart_campus_system.demo.dto;

import com.smart_campus_system.demo.model.TicketStatus;

import jakarta.validation.constraints.NotNull;

public class TicketStatusUpdateRequest {

	@NotNull
	private TicketStatus status;

	public TicketStatus getStatus() {
		return status;
	}

	public void setStatus(TicketStatus status) {
		this.status = status;
	}
}
