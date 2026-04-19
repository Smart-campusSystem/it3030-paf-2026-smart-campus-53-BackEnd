package com.smart_campus_system.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    private String type; // INFO, WARNING, ALERT

    private boolean isRead = false;

    private String userEmail;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Keeping ticketId for backward compatibility
    private Long ticketId;

    // Tracks which admin created this notification (null = system-generated)
    private String createdByEmail;
}
