package com.smart_campus_system.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smart_campus_system.demo.model.TicketComment;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {
}
