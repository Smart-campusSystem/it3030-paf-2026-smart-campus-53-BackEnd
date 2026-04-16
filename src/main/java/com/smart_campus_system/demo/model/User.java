package com.smart_campus_system.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private String email;

	private String firstName;
	private String lastName;

	@Enumerated(EnumType.STRING)
	private Role role;

	/** LOCAL users authenticate with passwordHash; OAuth users leave null. */
	private String passwordHash;

	private String provider;
	private String providerId;

	private String profileImageUrl;

	@Builder.Default
	private boolean active = true;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@PrePersist
	public void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	public void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
}
