package com.smart_campus_system.demo.repository;

import com.smart_campus_system.demo.model.Resource;
import com.smart_campus_system.demo.model.ResourceType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
	@Query("""
			select r
			from Resource r
			where (:type is null or r.type = :type)
			  and (:location is null or lower(r.location) like lower(concat('%', :location, '%')))
			  and (:minCapacity is null or r.capacity >= :minCapacity)
			""")
	List<Resource> search(
			@Param("type") ResourceType type,
			@Param("location") String location,
			@Param("minCapacity") Integer minCapacity
	);
}
