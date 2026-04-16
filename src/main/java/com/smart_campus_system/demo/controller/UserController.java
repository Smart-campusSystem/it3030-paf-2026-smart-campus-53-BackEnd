package com.smart_campus_system.demo.controller;

import com.smart_campus_system.demo.dto.UserCreateRequest;
import com.smart_campus_system.demo.dto.UserResponse;
import com.smart_campus_system.demo.dto.UserUpdateRequest;
import com.smart_campus_system.demo.exception.ApiException;
import com.smart_campus_system.demo.security.UserPrincipal;
import com.smart_campus_system.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/me")
	public UserResponse me() {
		return userService.currentProfile();
	}

	@PutMapping("/me")
	public UserResponse updateMe(@Valid @RequestBody UserUpdateRequest req) {
		return userService.updateUser(currentUserId(), req, false);
	}

	@PostMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public UserResponse uploadProfileImage(@RequestPart("file") MultipartFile file) {
		return userService.uploadProfileImage(currentUserId(), file);
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public List<UserResponse> listUsers() {
		return userService.findAll();
	}

	@GetMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	public UserResponse getUser(@PathVariable Long id) {
		return userService.getByIdForCaller(id);
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public UserResponse createUser(@Valid @RequestBody UserCreateRequest req) {
		return userService.createUser(req);
	}

	@PutMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest req) {
		return userService.updateUser(id, req, isAdmin());
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteUser(@PathVariable Long id) {
		userService.deleteUser(id);
	}

	private static Long currentUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal p)) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated");
		}
		return p.id();
	}

	private static boolean isAdmin() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			return false;
		}
		return auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
	}
}
