package com.smart_campus_system.demo.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smart_campus_system.demo.dto.UserSummaryResponse;
import com.smart_campus_system.demo.model.Role;
import com.smart_campus_system.demo.repository.UserRepository;

@RestController
@RequestMapping("/api/technicians")
public class TechnicianController {

	private final UserRepository userRepository;

	public TechnicianController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public List<UserSummaryResponse> listTechnicians() {
		return userRepository.findByRole(Role.TECHNICIAN).stream()
				.map(UserSummaryResponse::fromEntity)
				.toList();
	}
}
