package com.smart_campus_system.demo.service;

import java.time.LocalDateTime;

import com.smart_campus_system.demo.dto.AuthLoginRequest;
import com.smart_campus_system.demo.dto.AuthRegisterRequest;
import com.smart_campus_system.demo.dto.UserCreateRequest;
import com.smart_campus_system.demo.exception.ApiException;
import com.smart_campus_system.demo.model.Otp;
import com.smart_campus_system.demo.model.Role;
import com.smart_campus_system.demo.repository.OtpRepository;
import com.smart_campus_system.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
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
	void registerAssignsUserRole() {
		seedOtp("new-user@example.com", "200001");
		AuthRegisterRequest req = new AuthRegisterRequest();
		req.setEmail("new-user@example.com");
		req.setFirstName("N");
		req.setLastName("U");
		req.setPassword("password12");
		req.setOtp("200001");

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
	void loginWithPasswordWorksAfterGoogleLinkedToSameEmail() {
		seedOtp("hybrid@example.com", "200002");
		AuthRegisterRequest reg = new AuthRegisterRequest();
		reg.setEmail("hybrid@example.com");
		reg.setFirstName("H");
		reg.setLastName("Y");
		reg.setPassword("password12");
		reg.setOtp("200002");
		userService.registerLocal(reg);

		userService.upsertFromOAuth("hybrid@example.com", "G", "User", "google", "google-sub-123");

		AuthLoginRequest login = new AuthLoginRequest();
		login.setEmail("hybrid@example.com");
		login.setPassword("password12");
		var res = userService.loginLocal(login);
		assertThat(res.getAccessToken()).isNotBlank();
	}

	@Test
	void loginWithPasswordFailsForOAuthOnlyUser() {
		userService.upsertFromOAuth("oauthonly@example.com", "O", "Auth", "google", "sub-999");

		AuthLoginRequest login = new AuthLoginRequest();
		login.setEmail("oauthonly@example.com");
		login.setPassword("any-password");
		assertThatThrownBy(() -> userService.loginLocal(login))
				.isInstanceOf(BadCredentialsException.class);
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
