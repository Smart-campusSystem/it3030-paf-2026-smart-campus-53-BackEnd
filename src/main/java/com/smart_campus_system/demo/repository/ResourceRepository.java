package com.smart_campus_system.demo.repository;

import com.smart_campus_system.demo.model.Resource;
import com.smart_campus_system.demo.model.ResourceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
    List<Resource> findByStatusOrderByNameAsc(ResourceStatus status);
}
