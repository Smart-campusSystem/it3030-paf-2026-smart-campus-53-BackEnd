package com.smart_campus_system.demo.dto;

import com.smart_campus_system.demo.model.ResourceStatus;
import com.smart_campus_system.demo.model.ResourceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ResourceCreateRequest {
	@NotBlank
	private String name;

	@NotNull
	private ResourceType type;

	@Min(0)
	private Integer capacity;

	@NotBlank
	private String location;

	/** Optional on create; server applies a default when omitted or blank. */
	private String availability;

	@NotNull
	private ResourceStatus status;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ResourceType getType() {
		return type;
	}

	public void setType(ResourceType type) {
		this.type = type;
	}

	public Integer getCapacity() {
		return capacity;
	}

	public void setCapacity(Integer capacity) {
		this.capacity = capacity;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getAvailability() {
		return availability;
	}

	public void setAvailability(String availability) {
		this.availability = availability;
	}

	public ResourceStatus getStatus() {
		return status;
	}

	public void setStatus(ResourceStatus status) {
		this.status = status;
	}
}

