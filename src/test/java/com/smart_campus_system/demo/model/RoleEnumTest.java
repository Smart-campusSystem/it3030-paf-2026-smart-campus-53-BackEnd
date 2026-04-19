package com.smart_campus_system.demo.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoleEnumTest {

	@Test
	void containsExpectedRoles() {
		assertThat(Role.values()).containsExactly(Role.USER, Role.ADMIN, Role.TECHNICIAN);
	}
}
