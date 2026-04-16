package com.smart_campus_system.demo.security;

import com.smart_campus_system.demo.model.Role;
import com.smart_campus_system.demo.model.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

	private final JwtUtil jwtUtil = new JwtUtil("test-jwt-secret-must-be-at-least-32-chars!!", 3_600_000L);

	@Test
	void generatesAndParsesTokenWithRoleClaim() {
		User user = User.builder()
				.id(42L)
				.email("jwt-user@example.com")
				.role(Role.TECHNICIAN)
				.build();

		String token = jwtUtil.generateToken(user);
		Claims claims = jwtUtil.parseAndValidate(token);

		assertThat(claims.getSubject()).isEqualTo("42");
		assertThat(claims.get("email", String.class)).isEqualTo("jwt-user@example.com");
		assertThat(claims.get("role", String.class)).isEqualTo("TECHNICIAN");
	}
}
