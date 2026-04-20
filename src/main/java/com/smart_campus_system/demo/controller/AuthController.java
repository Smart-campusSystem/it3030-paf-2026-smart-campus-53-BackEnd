package com.smart_campus_system.demo.controller;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smart_campus_system.demo.dto.LoginRequest;
import com.smart_campus_system.demo.dto.LoginResponse;
import com.smart_campus_system.demo.exception.NotFoundException;
import com.smart_campus_system.demo.repository.UserRepository;
import com.smart_campus_system.demo.security.JwtUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final JwtUtil jwtUtil;

	public AuthController(
			AuthenticationManager authenticationManager,
			UserRepository userRepository,
			JwtUtil jwtUtil) {
		this.authenticationManager = authenticationManager;
		this.userRepository = userRepository;
		this.jwtUtil = jwtUtil;
	}

	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
		var user = userRepository.findByUsername(request.getUsername())
				.orElseThrow(() -> new NotFoundException("User not found"));
		String token = jwtUtil.generateToken(user);
		return new LoginResponse(token, user.getUsername(), user.getRole().name(), user.getId());
	}
}
