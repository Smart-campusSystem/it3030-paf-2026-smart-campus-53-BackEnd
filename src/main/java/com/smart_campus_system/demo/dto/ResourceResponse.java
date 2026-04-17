package com.smart_campus_system.demo.dto;

import com.smart_campus_system.demo.model.ResourceStatus;
import com.smart_campus_system.demo.model.ResourceType;

public record ResourceResponse(
		Long id,
		String name,
		ResourceType type,
		Integer capacity,
		String location,
		String availability,
		ResourceStatus status
) {}

