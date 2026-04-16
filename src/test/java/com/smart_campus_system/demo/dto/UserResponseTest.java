package com.smart_campus_system.demo.dto;

import com.smart_campus_system.demo.model.Role;
import com.smart_campus_system.demo.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserResponseTest {

	@Test
	void fromMapsEntityFields() {
		User u = User.builder()
				.id(9L)
				.email("map@example.com")
				.firstName("F")
				.lastName("L")
				.role(Role.USER)
				.provider("LOCAL")
				.profileImageUrl("http://cdn/p.png")
				.active(true)
				.createdAt(LocalDateTime.of(2026, 1, 1, 12, 0))
				.updatedAt(LocalDateTime.of(2026, 1, 2, 12, 0))
				.build();

		UserResponse r = UserResponse.from(u);

		assertThat(r.getId()).isEqualTo(9L);
		assertThat(r.getEmail()).isEqualTo("map@example.com");
		assertThat(r.getRole()).isEqualTo(Role.USER);
		assertThat(r.getProfileImageUrl()).isEqualTo("http://cdn/p.png");
	}
}
