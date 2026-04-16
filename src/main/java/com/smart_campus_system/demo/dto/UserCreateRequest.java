package com.smart_campus_system.demo.dto;

import com.smart_campus_system.demo.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateRequest {
	@NotBlank
	@Email
	private String email;

	@NotBlank
	@Size(max = 120)
	private String firstName;

	@NotBlank
	@Size(max = 120)
	private String lastName;

	private Role role;

	/** Required when creating a LOCAL user (admin flow). Ignored for OAuth-only rows. */
	@Size(min = 8, max = 128)
	private String password;
}
