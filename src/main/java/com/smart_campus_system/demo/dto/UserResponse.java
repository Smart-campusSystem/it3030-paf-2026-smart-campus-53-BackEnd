package com.smart_campus_system.demo.dto;

import com.smart_campus_system.demo.model.Role;
import com.smart_campus_system.demo.model.User;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class UserResponse {
	Long id;
	String email;
	String firstName;
	String lastName;
	Role role;
	String provider;
	String profileImageUrl;
	boolean active;
	LocalDateTime createdAt;
	LocalDateTime updatedAt;

	public static UserResponse from(User u) {
		return UserResponse.builder()
				.id(u.getId())
				.email(u.getEmail())
				.firstName(u.getFirstName())
				.lastName(u.getLastName())
				.role(u.getRole())
				.provider(u.getProvider())
				.profileImageUrl(u.getProfileImageUrl())
				.active(u.isActive())
				.createdAt(u.getCreatedAt())
				.updatedAt(u.getUpdatedAt())
				.build();
	}
}
