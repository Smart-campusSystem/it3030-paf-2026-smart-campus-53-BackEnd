package com.smart_campus_system.demo.service;

import com.smart_campus_system.demo.dto.AuthRegisterRequest;
import com.smart_campus_system.demo.dto.UserCreateRequest;
import com.smart_campus_system.demo.exception.ApiException;
import com.smart_campus_system.demo.model.Role;
import com.smart_campus_system.demo.model.User;
import com.smart_campus_system.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserServiceIntegrationTest {

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Test
	void registerAssignsUserRole() {
		AuthRegisterRequest req = new AuthRegisterRequest();
		req.setEmail("new-user@example.com");
		req.setFirstName("N");
		req.setLastName("U");
		req.setPassword("password12");

		var res = userService.registerLocal(req);
		assertThat(res.getUser().getRole()).isEqualTo(Role.USER);
		assertThat(userRepository.findByEmail("new-user@example.com")).isPresent();
	}

	@Test
	void createUserRequiresPassword() {
		UserCreateRequest req = new UserCreateRequest();
		req.setEmail("nopass@example.com");
		req.setFirstName("A");
		req.setLastName("B");
		req.setRole(Role.TECHNICIAN);

		assertThatThrownBy(() -> userService.createUser(req))
				.isInstanceOf(ApiException.class)
				.hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
	}

	@Test
	void createUserDefaultsToUserRoleWhenOmitted() {
		UserCreateRequest req = new UserCreateRequest();
		req.setEmail("staff@example.com");
		req.setFirstName("S");
		req.setLastName("T");
		req.setPassword("password12");

		var created = userService.createUser(req);
		assertThat(created.getRole()).isEqualTo(Role.USER);
	}

	@Test
	void createUserCanAssignTechnician() {
		UserCreateRequest req = new UserCreateRequest();
		req.setEmail("tech@example.com");
		req.setFirstName("T");
		req.setLastName("C");
		req.setRole(Role.TECHNICIAN);
		req.setPassword("password12");

		var created = userService.createUser(req);
		assertThat(created.getRole()).isEqualTo(Role.TECHNICIAN);
	}
}
