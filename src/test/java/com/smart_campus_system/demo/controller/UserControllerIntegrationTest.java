package com.smart_campus_system.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart_campus_system.demo.dto.UserCreateRequest;
import com.smart_campus_system.demo.model.Role;
import com.smart_campus_system.demo.model.User;
import com.smart_campus_system.demo.repository.UserRepository;
import com.smart_campus_system.demo.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtUtil jwtUtil;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private String adminToken;
	private String userToken;

	@BeforeEach
	void seedUsers() {
		User admin = userRepository.save(User.builder()
				.email("admin-dash@example.com")
				.firstName("Ad")
				.lastName("Min")
				.role(Role.ADMIN)
				.provider("LOCAL")
				.passwordHash(passwordEncoder.encode("adminpass12"))
				.active(true)
				.build());
		adminToken = jwtUtil.generateToken(admin);

		User plain = userRepository.save(User.builder()
				.email("plain-user@example.com")
				.firstName("P")
				.lastName("U")
				.role(Role.USER)
				.provider("LOCAL")
				.passwordHash(passwordEncoder.encode("userpass12"))
				.active(true)
				.build());
		userToken = jwtUtil.generateToken(plain);
	}

	@Test
	void adminCanCreateTechnician() throws Exception {
		UserCreateRequest req = new UserCreateRequest();
		req.setEmail("new-tech@example.com");
		req.setFirstName("Tech");
		req.setLastName("Nician");
		req.setRole(Role.TECHNICIAN);
		req.setPassword("password12");

		mockMvc.perform(post("/api/users")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("TECHNICIAN"));
	}

	@Test
	void adminCanCreateAnotherAdmin() throws Exception {
		UserCreateRequest req = new UserCreateRequest();
		req.setEmail("other-admin@example.com");
		req.setFirstName("A");
		req.setLastName("B");
		req.setRole(Role.ADMIN);
		req.setPassword("password12");

		mockMvc.perform(post("/api/users")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("ADMIN"));
	}

	@Test
	void plainUserCannotCreateTechnician() throws Exception {
		UserCreateRequest req = new UserCreateRequest();
		req.setEmail("blocked-tech@example.com");
		req.setFirstName("X");
		req.setLastName("Y");
		req.setRole(Role.TECHNICIAN);
		req.setPassword("password12");

		mockMvc.perform(post("/api/users")
						.header("Authorization", "Bearer " + userToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isForbidden());
	}

	@Test
	void plainUserCannotListAllUsers() throws Exception {
		mockMvc.perform(get("/api/users").header("Authorization", "Bearer " + userToken))
				.andExpect(status().isForbidden());
	}

	@Test
	void adminCanListUsers() throws Exception {
		mockMvc.perform(get("/api/users").header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}
}
