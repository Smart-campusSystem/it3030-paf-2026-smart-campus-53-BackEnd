package com.smart_campus_system.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart_campus_system.demo.model.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

	/** Loads submitter and assignee in one round trip (avoids N+1 and DISTINCT+FETCH edge cases). */
	@EntityGraph(attributePaths = { "submitter", "assignedTechnician" })
	List<Ticket> findAllByOrderByCreatedAtDesc();

	@Query("""
			SELECT t FROM Ticket t
			WHERE t.submitter.id = :userId
			   OR (t.submitter IS NULL AND lower(t.contactEmail) = lower(:email))
			ORDER BY t.createdAt DESC
			""")
	List<Ticket> findMine(@Param("userId") Long userId, @Param("email") String email);
}
