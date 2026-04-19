package com.smart_campus_system.demo.config;

import com.smart_campus_system.demo.model.Role;
import com.smart_campus_system.demo.model.User;
import com.smart_campus_system.demo.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inserts one LOCAL user per {@link Role} when enabled. Skips emails that already exist.
 */
@Component
@ConditionalOnProperty(name = "app.seed.sample-users", havingValue = "true")
public class SampleUserDataLoader implements ApplicationRunner {

	private static final String SHARED_PASSWORD = "SamplePass12";

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public SampleUserDataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		String hash = passwordEncoder.encode(SHARED_PASSWORD);
		seedIfMissing("sample-admin@example.com", "Sample", "Admin", Role.ADMIN, hash);
		seedIfMissing("sample-user@example.com", "Sample", "User", Role.USER, hash);
		seedIfMissing("sample-technician@example.com", "Sample", "Technician", Role.TECHNICIAN, hash);
	}

	private void seedIfMissing(String email, String firstName, String lastName, Role role, String passwordHash) {
		if (userRepository.existsByEmail(email)) {
			return;
		}
		userRepository.save(User.builder()
				.email(email)
				.firstName(firstName)
				.lastName(lastName)
				.role(role)
				.provider("LOCAL")
				.passwordHash(passwordHash)
				.active(true)
				.build());
	}
}
