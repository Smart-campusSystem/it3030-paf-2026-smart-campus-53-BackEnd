package com.smart_campus_system.demo.controller;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.smart_campus_system.demo.model.Otp;
import com.smart_campus_system.demo.repository.OtpRepository;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private OtpRepository otpRepository;

	private void seedOtp(String email, String code) {
		String e = email.trim().toLowerCase();
		otpRepository.deleteByEmail(e);
		otpRepository.save(Otp.builder()
				.email(e)
				.code(code)
				.expiresAt(LocalDateTime.now().plusMinutes(5))
				.build());
	}

	@Test
	void registerReturnsUserWithRoleUser() throws Exception {
		seedOtp("register-flow@example.com", "100001");
		String body = """
				{"email":"register-flow@example.com","firstName":"F","lastName":"L","password":"password12","otp":"100001"}
				""";
		mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.user.role").value("USER"))
				.andExpect(jsonPath("$.accessToken").isString());
	}

	@Test
	void loginWithEmailAndPasswordSucceedsAfterRegister() throws Exception {
		seedOtp("login-flow@example.com", "100002");
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"login-flow@example.com","firstName":"A","lastName":"B","password":"password12","otp":"100002"}
								"""))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"login-flow@example.com","password":"password12"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.user.email").value("login-flow@example.com"));
	}

	@Test
	void duplicateRegistrationReturnsConflict() throws Exception {
		seedOtp("dup@example.com", "100003");
		String body = """
				{"email":"dup@example.com","firstName":"D","lastName":"U","password":"password12","otp":"100003"}
				""";
		mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isOk());
		mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message", containsString("Email")));
	}
}
