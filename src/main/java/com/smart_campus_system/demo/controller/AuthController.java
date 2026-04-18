package com.smart_campus_system.demo.controller;

import com.smart_campus_system.demo.dto.AuthLoginRequest;
import com.smart_campus_system.demo.dto.AuthRegisterRequest;
import com.smart_campus_system.demo.dto.AuthSendOtpRequest;
import com.smart_campus_system.demo.dto.AuthResponse;
import com.smart_campus_system.demo.service.UserService;
import com.smart_campus_system.demo.service.OtpService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final UserService userService;
	private final OtpService otpService;

	public AuthController(UserService userService, OtpService otpService) {
		this.userService = userService;
		this.otpService = otpService;
	}

	@PostMapping("/send-otp")
	public ResponseEntity<?> sendOtp(@Valid @RequestBody AuthSendOtpRequest req) {
		otpService.generateAndSendOtp(req.getEmail());
		return ResponseEntity.ok().body("{\"message\": \"OTP sent successfully\"}");
	}

	@PostMapping("/register")
	public AuthResponse register(@Valid @RequestBody AuthRegisterRequest req) {
		return userService.registerLocal(req);
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody AuthLoginRequest req) {
		return userService.loginLocal(req);
	}
}
