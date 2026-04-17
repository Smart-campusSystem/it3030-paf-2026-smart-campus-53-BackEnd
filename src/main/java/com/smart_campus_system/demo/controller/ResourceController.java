package com.smart_campus_system.demo.controller;

import com.smart_campus_system.demo.dto.ResourceCreateRequest;
import com.smart_campus_system.demo.dto.ResourceResponse;
import com.smart_campus_system.demo.dto.ResourceUpdateRequest;
import com.smart_campus_system.demo.model.ResourceType;
import com.smart_campus_system.demo.service.ResourceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {
	private final ResourceService resourceService;

	public ResourceController(ResourceService resourceService) {
		this.resourceService = resourceService;
	}

	@PostMapping
	public ResponseEntity<ResourceResponse> create(@Valid @RequestBody ResourceCreateRequest req) {
		return ResponseEntity.status(HttpStatus.CREATED).body(resourceService.create(req));
	}

	@GetMapping
	public List<ResourceResponse> search(
			@RequestParam(required = false) ResourceType type,
			@RequestParam(required = false) String location,
			@RequestParam(required = false) Integer minCapacity
	) {
		return resourceService.search(type, location, minCapacity);
	}

	@GetMapping("/{id}")
	public ResourceResponse getById(@PathVariable Long id) {
		return resourceService.getById(id);
	}

	@PatchMapping("/{id}")
	public ResourceResponse update(@PathVariable Long id, @Valid @RequestBody ResourceUpdateRequest req) {
		return resourceService.update(id, req);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		resourceService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
