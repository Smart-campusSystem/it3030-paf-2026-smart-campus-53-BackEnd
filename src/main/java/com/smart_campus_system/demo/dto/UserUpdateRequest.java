package com.smart_campus_system.demo.dto;

import com.smart_campus_system.demo.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {
	@Email
	private String email;

	@Size(max = 120)
	private String firstName;

	@Size(max = 120)
	private String lastName;

	private Role role;

	private Boolean active;

	@Size(min = 8, max = 128)
	private String password;
}
