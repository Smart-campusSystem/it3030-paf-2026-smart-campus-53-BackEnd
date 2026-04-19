package com.smart_campus_system.demo.service;

import com.smart_campus_system.demo.dto.*;
import com.smart_campus_system.demo.exception.ApiException;
import com.smart_campus_system.demo.model.Role;
import com.smart_campus_system.demo.model.User;
import com.smart_campus_system.demo.repository.UserRepository;
import com.smart_campus_system.demo.security.JwtUtil;
import com.smart_campus_system.demo.security.UserPrincipal;
import com.smart_campus_system.demo.storage.ProfileImageStorage;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final ProfileImageStorage profileImageStorage;
	private final JwtUtil jwtUtil;
	private final OtpService otpService;
	private final NotificationProducer notificationProducer;

	public UserService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			ProfileImageStorage profileImageStorage,
			JwtUtil jwtUtil,
			OtpService otpService,
			NotificationProducer notificationProducer) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.profileImageStorage = profileImageStorage;
		this.jwtUtil = jwtUtil;
		this.otpService = otpService;
		this.notificationProducer = notificationProducer;
	}

	private static String normEmail(String email) {
		return email.trim().toLowerCase();
	}

	public AuthResponse registerLocal(AuthRegisterRequest req) {
		String email = normEmail(req.getEmail());
		if (userRepository.existsByEmail(email)) {
			throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
		}
		if (!otpService.verifyOtp(email, req.getOtp())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP");
		}
		User user = User.builder()
				.email(email)
				.firstName(req.getFirstName().trim())
				.lastName(req.getLastName().trim())
				.role(Role.USER)
				.provider("LOCAL")
				.passwordHash(passwordEncoder.encode(req.getPassword()))
				.active(true)
				.build();
		user = userRepository.save(user);

		// Trigger Notification Example exactly as requested!
		com.smart_campus_system.demo.model.Notification n = com.smart_campus_system.demo.model.Notification.builder()
				.message("Welcome to system!")
				.type("INFO")
				.userEmail(user.getEmail())
				.isRead(false)
				.createdAt(java.time.LocalDateTime.now())
				.build();
		notificationProducer.send(n);

		return AuthResponse.builder()
				.accessToken(jwtUtil.generateToken(user))
				.user(toUserResponse(user))
				.build();
	}

	public AuthResponse loginLocal(AuthLoginRequest req) {
		String email = normEmail(req.getEmail());
		User user = userRepository.findByEmail(email).orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
		if (!user.isActive()) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Account is disabled");
		}
		// Allow password login whenever a password is set, including after Google is linked
		// (upsertFromOAuth sets provider to the OAuth registration id, not "LOCAL").
		if (user.getPasswordHash() == null) {
			throw new BadCredentialsException("Invalid credentials");
		}
		if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
			throw new BadCredentialsException("Invalid credentials");
		}
		return AuthResponse.builder()
				.accessToken(jwtUtil.generateToken(user))
				.user(toUserResponse(user))
				.build();
	}

	@Transactional
	public User upsertFromOAuth(String email, String firstName, String lastName, String provider, String providerId) {
		String e = normEmail(email);
		var byProvider = userRepository.findByProviderAndProviderId(provider, providerId);
		if (byProvider.isPresent()) {
			User u = byProvider.get();
			u.setEmail(e);
			if (firstName != null && !firstName.isBlank()) {
				u.setFirstName(firstName.trim());
			}
			if (lastName != null && !lastName.isBlank()) {
				u.setLastName(lastName.trim());
			}
			return userRepository.save(u);
		}
		return userRepository.findByEmail(e)
				.map(u -> {
					u.setProvider(provider);
					u.setProviderId(providerId);
					if (firstName != null && !firstName.isBlank()) {
						u.setFirstName(firstName.trim());
					}
					if (lastName != null && !lastName.isBlank()) {
						u.setLastName(lastName.trim());
					}
					return userRepository.save(u);
				})
				.orElseGet(() -> userRepository.save(User.builder()
						.email(e)
						.firstName(firstName != null ? firstName.trim() : "")
						.lastName(lastName != null ? lastName.trim() : "")
						.role(Role.USER)
						.provider(provider)
						.providerId(providerId)
						.active(true)
						.build()));
	}

	@Transactional(readOnly = true)
	public List<UserResponse> findAll() {
		return userRepository.findAll().stream().map(this::toUserResponse).toList();
	}

	@Transactional(readOnly = true)
	public UserResponse findById(Long id) {
		return userRepository.findById(id).map(this::toUserResponse).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
	}

	@Transactional(readOnly = true)
	public UserResponse getByIdForCaller(Long id) {
		var auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal p)) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated");
		}
		boolean admin = auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
		if (!admin && !p.id().equals(id)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Not allowed to view this user");
		}
		return findById(id);
	}

	@Transactional(readOnly = true)
	public UserResponse currentProfile() {
		return toUserResponse(loadCurrentUserEntity());
	}

	/**
	 * Admin-only: create LOCAL users. {@code ADMIN} and {@code TECHNICIAN} must be created here
	 * (not via public registration). Defaults to {@link Role#USER} when role is omitted.
	 */
	@Transactional
	public UserResponse createUser(UserCreateRequest req) {
		String email = normEmail(req.getEmail());
		if (userRepository.existsByEmail(email)) {
			throw new ApiException(HttpStatus.CONFLICT, "Email already in use");
		}
		if (req.getPassword() == null || req.getPassword().isBlank()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Password is required when creating a local user");
		}
		Role role = req.getRole() != null ? req.getRole() : Role.USER;
		User.UserBuilder b = User.builder()
				.email(email)
				.firstName(req.getFirstName().trim())
				.lastName(req.getLastName().trim())
				.role(role)
				.provider("LOCAL")
				.passwordHash(passwordEncoder.encode(req.getPassword()))
				.active(true);
		User saved = userRepository.save(b.build());
		return toUserResponse(saved);
	}

	@Transactional
	public UserResponse updateUser(Long id, UserUpdateRequest req, boolean asAdmin) {
		User user = userRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
		if (!asAdmin) {
			assertSelfOrThrow(id);
		}
		if (req.getEmail() != null && !req.getEmail().isBlank()) {
			String newEmail = normEmail(req.getEmail());
			if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
				throw new ApiException(HttpStatus.CONFLICT, "Email already in use");
			}
			user.setEmail(newEmail);
		}
		if (req.getFirstName() != null) {
			user.setFirstName(req.getFirstName().trim());
		}
		if (req.getLastName() != null) {
			user.setLastName(req.getLastName().trim());
		}
		if (asAdmin && req.getRole() != null) {
			user.setRole(req.getRole());
		}
		if (asAdmin && req.getActive() != null) {
			user.setActive(req.getActive());
		}
		if (req.getPassword() != null && !req.getPassword().isBlank()) {
			user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
			if (user.getProvider() == null || user.getProvider().isBlank()) {
				user.setProvider("LOCAL");
			}
		}
		return toUserResponse(userRepository.save(user));
	}

	@Transactional
	public void deleteUser(Long id) {
		if (!userRepository.existsById(id)) {
			throw new ApiException(HttpStatus.NOT_FOUND, "User not found");
		}
		userRepository.deleteById(id);
	}

	@Transactional
	public UserResponse uploadProfileImage(Long userId, MultipartFile file) {
		assertSelfOrThrow(userId);
		if (file.isEmpty()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "File is required");
		}
		User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
		String url = profileImageStorage.save(userId, file);
		user.setProfileImageUrl(url);
		return toUserResponse(userRepository.save(user));
	}

	private UserResponse toUserResponse(User user) {
		String displayUrl = profileImageStorage.resolveDisplayUrl(user.getProfileImageUrl());
		return UserResponse.from(user, displayUrl);
	}

	public User loadCurrentUserEntity() {
		var auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal p)) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated");
		}
		return userRepository.findById(p.id()).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
	}

	private void assertSelfOrThrow(Long userId) {
		var auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal p)) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated");
		}
		boolean admin = auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
		if (!admin && !p.id().equals(userId)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Not allowed to modify this user");
		}
	}
}
