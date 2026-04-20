package com.smart_campus_system.demo.dto;

import jakarta.validation.constraints.NotNull;

public class AssignTechnicianRequest {

	@NotNull
	private Long technicianId;

	public Long getTechnicianId() {
		return technicianId;
	}

	public void setTechnicianId(Long technicianId) {
		this.technicianId = technicianId;
	}
}
