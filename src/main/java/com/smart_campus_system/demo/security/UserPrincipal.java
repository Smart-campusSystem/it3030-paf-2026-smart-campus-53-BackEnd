package com.smart_campus_system.demo.security;

import org.springframework.security.core.AuthenticatedPrincipal;

/**
 * JWT principal. {@link AuthenticatedPrincipal#getName()} must be the user email so
 * {@code authentication.getName()} matches {@code DatabaseUserDetailsService}-style lookups.
 */
public record UserPrincipal(Long id, String email) implements AuthenticatedPrincipal {
	@Override
	public String getName() {
		return email != null ? email : "";
	}
}
