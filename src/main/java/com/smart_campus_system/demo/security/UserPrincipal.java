package com.smart_campus_system.demo.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * JWT-backed principal. Implements {@link UserDetails} so
 * {@link org.springframework.security.core.Authentication#getName()}
 * returns the email (via {@link #getUsername()}), not {@code toString()}.
 * Booking and other controllers use {@code getName()}
 * to resolve the current user's email.
 */
public record UserPrincipal(Long id, String email) implements UserDetails {

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.emptyList();
	}

	@Override
	public String getPassword() {
		return "";
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
