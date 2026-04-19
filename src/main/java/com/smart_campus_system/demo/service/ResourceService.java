package com.smart_campus_system.demo.service;

import com.smart_campus_system.demo.dto.ResourceCreateRequest;
import com.smart_campus_system.demo.dto.ResourceResponse;
import com.smart_campus_system.demo.dto.ResourceUpdateRequest;
import com.smart_campus_system.demo.exception.NotFoundException;
import com.smart_campus_system.demo.model.Resource;
import com.smart_campus_system.demo.model.ResourceType;
import com.smart_campus_system.demo.repository.ResourceRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResourceService {
	private final ResourceRepository resourceRepository;

	public ResourceService(ResourceRepository resourceRepository) {
		this.resourceRepository = resourceRepository;
	}

	@Transactional
	public ResourceResponse create(ResourceCreateRequest req) {
		validateCapacityRule(req.getType(), req.getCapacity());

		Resource r = new Resource();
		r.setName(req.getName().trim());
		r.setType(req.getType());
		r.setCapacity(req.getCapacity());
		r.setLocation(req.getLocation().trim());
		r.setAvailability(resolveAvailabilityForCreate(req.getAvailability()));
		r.setStatus(req.getStatus());

		Resource saved = resourceRepository.save(r);
		return toResponse(saved);
	}

	@Transactional(readOnly = true)
	public List<ResourceResponse> search(ResourceType type, String location, Integer minCapacity) {
		return resourceRepository.search(type, blankToNull(location), minCapacity).stream()
				.map(ResourceService::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public ResourceResponse getById(Long id) {
		return toResponse(getEntity(id));
	}

	@Transactional
	public ResourceResponse update(Long id, ResourceUpdateRequest req) {
		Resource r = getEntity(id);

		ResourceType nextType = req.getType() != null ? req.getType() : r.getType();
		Integer nextCapacity = req.getCapacity() != null ? req.getCapacity() : r.getCapacity();
		validateCapacityRule(nextType, nextCapacity);

		if (req.getName() != null) r.setName(req.getName().trim());
		if (req.getType() != null) r.setType(req.getType());
		if (req.getCapacity() != null) r.setCapacity(req.getCapacity());
		if (req.getLocation() != null) r.setLocation(req.getLocation().trim());
		if (req.getAvailability() != null) r.setAvailability(req.getAvailability().trim());
		if (req.getStatus() != null) r.setStatus(req.getStatus());

		return toResponse(resourceRepository.save(r));
	}

	@Transactional
	public void delete(Long id) {
		if (!resourceRepository.existsById(id)) {
			throw new NotFoundException("Resource not found: " + id);
		}
		resourceRepository.deleteById(id);
	}

	private Resource getEntity(Long id) {
		return resourceRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Resource not found: " + id));
	}

	private static String resolveAvailabilityForCreate(String availability) {
		if (availability == null) {
			return "N/A";
		}
		String t = availability.trim();
		return t.isEmpty() ? "N/A" : t;
	}

	private static void validateCapacityRule(ResourceType type, Integer capacity) {
		if (type == ResourceType.EQUIPMENT) {
			return;
		}
		if (capacity == null) {
			throw new IllegalArgumentException("capacity is required for type " + type);
		}
	}

	private static String blankToNull(String s) {
		if (s == null) return null;
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}

	private static ResourceResponse toResponse(Resource r) {
		return new ResourceResponse(
				r.getId(),
				r.getName(),
				r.getType(),
				r.getCapacity(),
				r.getLocation(),
				r.getAvailability(),
				r.getStatus()
		);
	}
}
