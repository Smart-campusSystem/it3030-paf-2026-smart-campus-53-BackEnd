package com.smart_campus_system.demo.dto;

import com.smart_campus_system.demo.model.ResourceStatus;
import com.smart_campus_system.demo.model.ResourceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ResourceRequestDTO {

    @NotBlank(message = "name is required")
    private String name;

    @NotNull(message = "type is required")
    private ResourceType type;

    @NotNull(message = "capacity is required")
    @Min(value = 1, message = "capacity must be > 0")
    private Integer capacity;

    @NotBlank(message = "location is required")
    private String location;

    @NotNull(message = "status is required")
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

    public ResourceStatus getStatus() {
        return status;
    }

    public void setStatus(ResourceStatus status) {
        this.status = status;
    }
}

