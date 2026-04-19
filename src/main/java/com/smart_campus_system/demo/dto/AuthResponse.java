package com.smart_campus_system.demo.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthResponse {
	String accessToken;
	UserResponse user;
}
