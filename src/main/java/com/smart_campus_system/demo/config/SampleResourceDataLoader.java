package com.smart_campus_system.demo.config;

import com.smart_campus_system.demo.model.Resource;
import com.smart_campus_system.demo.model.ResourceStatus;
import com.smart_campus_system.demo.model.ResourceType;
import com.smart_campus_system.demo.repository.ResourceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads sample Module A resources once when the table is empty.
 * Human-readable catalogue types from the brief are mapped to {@link ResourceType}.
 */
@Component
public class SampleResourceDataLoader implements CommandLineRunner {

	private final ResourceRepository resourceRepository;

	public SampleResourceDataLoader(ResourceRepository resourceRepository) {
		this.resourceRepository = resourceRepository;
	}

	@Override
	@Transactional
	public void run(String... args) {
		if (resourceRepository.count() > 0) {
			return;
		}

		save("Main Auditorium", ResourceType.ROOM, 300, "Building A, Ground Floor",
				"08:00 AM - 08:00 PM", ResourceStatus.ACTIVE);
		save("Advanced Computing Lab", ResourceType.LAB, 50, "IT Faculty, 3rd Floor",
				"09:00 AM - 05:30 PM", ResourceStatus.ACTIVE);
		save("Epson Laser Projector #04", ResourceType.EQUIPMENT, null, "AV Resource Center",
				"08:00 AM - 04:00 PM", ResourceStatus.OUT_OF_SERVICE);
		save("Collaborative Space B", ResourceType.ROOM, 12, "Library, 2nd Floor",
				"24 Hours", ResourceStatus.ACTIVE);
		save("DSLR Camera Kit #01", ResourceType.EQUIPMENT, null, "Media Studio",
				"09:00 AM - 05:00 PM", ResourceStatus.ACTIVE);
		save("Chemistry Research Lab", ResourceType.LAB, 25, "Science Block, Room 102",
				"08:00 AM - 06:00 PM", ResourceStatus.ACTIVE);
	}

	private void save(
			String name,
			ResourceType type,
			Integer capacity,
			String location,
			String availability,
			ResourceStatus status
	) {
		Resource r = new Resource();
		r.setName(name);
		r.setType(type);
		r.setCapacity(capacity);
		r.setLocation(location);
		r.setAvailability(availability);
		r.setStatus(status);
		resourceRepository.save(r);
	}
}
