package com.smart_campus_system.demo.dto;

public class LoginResponse {

	private String token;
	private String username;
	private String role;
	private long userId;

	public LoginResponse(String token, String username, String role, long userId) {
		this.token = token;
		this.username = username;
		this.role = role;
		this.userId = userId;
	}

	public String getToken() {
		return token;
	}

	public String getUsername() {
		return username;
	}

	public String getRole() {
		return role;
	}

	public long getUserId() {
		return userId;
	}
}
