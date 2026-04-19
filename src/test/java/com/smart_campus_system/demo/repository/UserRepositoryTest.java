package com.smart_campus_system.demo.repository;

import com.smart_campus_system.demo.model.Role;
import com.smart_campus_system.demo.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@Test
	void findByEmailReturnsSavedUser() {
		User u = userRepository.save(User.builder()
				.email("repo-test@example.com")
				.firstName("R")
				.lastName("T")
				.role(Role.USER)
				.provider("LOCAL")
				.passwordHash("hash")
				.active(true)
				.build());

		assertThat(userRepository.findByEmail("repo-test@example.com")).contains(u);
	}
}
