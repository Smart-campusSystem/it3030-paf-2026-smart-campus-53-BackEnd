package com.smart_campus_system.demo.controller;

import com.smart_campus_system.demo.dto.AuthLoginRequest;
import com.smart_campus_system.demo.dto.AuthRegisterRequest;
import com.smart_campus_system.demo.dto.AuthResponse;
import com.smart_campus_system.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final UserService userService;

	public AuthController(UserService userService) {
		this.userService = userService;
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
