package com.smart_campus_system.demo.controller;

import com.smart_campus_system.demo.dto.ApiResponse;
import com.smart_campus_system.demo.dto.ResourceRequestDTO;
import com.smart_campus_system.demo.dto.ResourceResponseDTO;
import com.smart_campus_system.demo.service.ResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
@Slf4j
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ResourceResponseDTO>>> getAll() {
        List<ResourceResponseDTO> resources = resourceService.getAllResources();
        return ResponseEntity.ok(ApiResponse.ok(resources, "Resources fetched successfully"));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<ResourceResponseDTO>>> getActive() {
        List<ResourceResponseDTO> resources = resourceService.getActiveResources();
        return ResponseEntity.ok(ApiResponse.ok(resources, "Active resources fetched successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ResourceResponseDTO>> create(@Valid @RequestBody ResourceRequestDTO dto) {
        ResourceResponseDTO created = resourceService.createResource(dto);
        return ResponseEntity.status(201).body(ApiResponse.ok(created, "Resource created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ResourceResponseDTO>> update(@PathVariable Long id,
                                                                   @Valid @RequestBody ResourceRequestDTO dto) {
        ResourceResponseDTO updated = resourceService.updateResource(id, dto);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Resource updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Resource deleted successfully"));
    }
}
