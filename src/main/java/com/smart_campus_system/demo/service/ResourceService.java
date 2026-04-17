package com.smart_campus_system.demo.service;

import com.smart_campus_system.demo.dto.ResourceRequestDTO;
import com.smart_campus_system.demo.dto.ResourceResponseDTO;
import com.smart_campus_system.demo.exception.ResourceNotFoundException;
import com.smart_campus_system.demo.model.Resource;
import com.smart_campus_system.demo.model.ResourceStatus;
import com.smart_campus_system.demo.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;

    @Transactional(readOnly = true)
    public List<ResourceResponseDTO> getAllResources() {
        log.info("Fetching all resources");
        List<ResourceResponseDTO> list = resourceRepository.findAll().stream().map(this::toDto).toList();
        log.info("Fetching all resources done");
        return list;
    }

    @Transactional(readOnly = true)
    public List<ResourceResponseDTO> getActiveResources() {
        log.info("Fetching ACTIVE resources");
        List<ResourceResponseDTO> list =
                resourceRepository.findByStatusOrderByNameAsc(ResourceStatus.ACTIVE).stream().map(this::toDto).toList();
        log.info("Fetching ACTIVE resources done");
        return list;
    }

    @Transactional
    public ResourceResponseDTO createResource(ResourceRequestDTO dto) {
        log.info("Creating resource {}", dto.getName());
        Resource resource = new Resource();
        resource.setName(dto.getName().trim());
        resource.setType(dto.getType());
        resource.setCapacity(dto.getCapacity());
        resource.setLocation(dto.getLocation().trim());
        resource.setStatus(dto.getStatus());
        Resource saved = resourceRepository.save(resource);
        log.info("Created resource id={}", saved.getId());
        return toDto(saved);
    }

    @Transactional
    public ResourceResponseDTO updateResource(Long id, ResourceRequestDTO dto) {
        log.info("Updating resource id={}", id);
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));

        resource.setName(dto.getName().trim());
        resource.setType(dto.getType());
        resource.setCapacity(dto.getCapacity());
        resource.setLocation(dto.getLocation().trim());
        resource.setStatus(dto.getStatus());

        Resource saved = resourceRepository.save(resource);
        log.info("Updated resource id={}", id);
        return toDto(saved);
    }

    @Transactional
    public void deleteResource(Long id) {
        log.info("Deleting resource id={}", id);
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
        resourceRepository.delete(resource);
        log.info("Deleted resource id={}", id);
    }

    private ResourceResponseDTO toDto(Resource r) {
        ResourceResponseDTO dto = new ResourceResponseDTO();
        dto.setResourceId(r.getId());
        dto.setName(r.getName());
        dto.setType(r.getType());
        dto.setCapacity(r.getCapacity());
        dto.setLocation(r.getLocation());
        dto.setStatus(r.getStatus());
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }
}
