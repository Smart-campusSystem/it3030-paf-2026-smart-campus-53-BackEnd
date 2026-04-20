package com.smart_campus_system.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smart_campus_system.demo.model.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
