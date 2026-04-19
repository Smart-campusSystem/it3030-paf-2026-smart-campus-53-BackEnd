package com.smart_campus_system.demo.repository;

import com.smart_campus_system.demo.model.User;
import com.smart_campus_system.demo.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	Optional<User> findByProviderAndProviderId(String provider, String providerId);

	List<User> findByRole(Role role);
}
