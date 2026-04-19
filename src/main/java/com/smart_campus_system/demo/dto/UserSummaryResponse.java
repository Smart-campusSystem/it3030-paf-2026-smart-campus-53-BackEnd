package com.smart_campus_system.demo.dto;

import com.smart_campus_system.demo.model.Role;
import com.smart_campus_system.demo.model.User;

public class UserSummaryResponse {

	private Long id;
	private String username;
	private String email;
	private Role role;

	public static UserSummaryResponse fromEntity(User user) {
		if (user == null) {
			return null;
		}
		UserSummaryResponse r = new UserSummaryResponse();
		r.id = user.getId();
		r.username = user.getUsername();
		r.email = user.getEmail();
		r.role = user.getRole();
		return r;
	}

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getEmail() {
		return email;
	}

	public Role getRole() {
		return role;
	}
}
