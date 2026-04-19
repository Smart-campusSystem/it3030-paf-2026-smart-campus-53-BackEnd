package com.smart_campus_system.demo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.smart_campus_system.demo.model.Role;
import com.smart_campus_system.demo.model.User;
import com.smart_campus_system.demo.repository.UserRepository;

@Configuration
public class DataInitializer {

	@Bean
	CommandLineRunner seedUsers(UserRepository users, PasswordEncoder encoder) {
		return args -> {
			if (users.count() > 0) {
				return;
			}
			users.save(user("admin", "admin@campus.edu", Role.ADMIN, encoder));
			users.save(user("tech1", "tech1@campus.edu", Role.TECHNICIAN, encoder));
			users.save(user("tech2", "tech2@campus.edu", Role.TECHNICIAN, encoder));
			users.save(user("student1", "student1@campus.edu", Role.USER, encoder));
		};
	}

	private static User user(String username, String email, Role role, PasswordEncoder encoder) {
		User u = new User();
		u.setFirstName(username);
		u.setEmail(email);
		u.setRole(role);
		u.setPasswordHash(encoder.encode("password"));
		return u;
	}
}
