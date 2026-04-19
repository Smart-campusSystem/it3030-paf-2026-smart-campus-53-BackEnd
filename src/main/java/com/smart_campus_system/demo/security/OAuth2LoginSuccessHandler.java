package com.smart_campus_system.demo.security;

import com.smart_campus_system.demo.model.User;
import com.smart_campus_system.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final UserService userService;
	private final JwtUtil jwtUtil;
	private final String redirectBase;

	public OAuth2LoginSuccessHandler(
			UserService userService,
			JwtUtil jwtUtil,
			@Value("${app.oauth2.frontend-redirect-url:http://localhost:5173/auth/callback}") String redirectBase) {
		this.userService = userService;
		this.jwtUtil = jwtUtil;
		this.redirectBase = redirectBase;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException {
		OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
		Map<String, Object> attrs = oauth2User.getAttributes();
		String email = (String) attrs.get("email");
		if (email == null || email.isBlank()) {
			getRedirectStrategy().sendRedirect(request, response, redirectBase + "?error=email_required");
			return;
		}
		String sub = String.valueOf(attrs.get("sub"));
		String given = (String) attrs.get("given_name");
		String family = (String) attrs.get("family_name");
		if (given == null && family == null) {
			String name = (String) attrs.get("name");
			if (name != null && !name.isBlank()) {
				String[] parts = name.trim().split("\\s+", 2);
				given = parts[0];
				family = parts.length > 1 ? parts[1] : "";
			}
		}
		if (given == null) {
			given = "";
		}
		if (family == null) {
			family = "";
		}
		String registrationId = resolveProvider(authentication);
		User user = userService.upsertFromOAuth(email, given, family, registrationId, sub);
		String token = jwtUtil.generateToken(user);
		String target = UriComponentsBuilder.fromUriString(redirectBase)
				.queryParam("token", token)
				.build()
				.toUriString();
		getRedirectStrategy().sendRedirect(request, response, target);
	}

	private static String resolveProvider(Authentication authentication) {
		if (authentication instanceof OAuth2AuthenticationToken token) {
			return token.getAuthorizedClientRegistrationId().toUpperCase();
		}
		return "OAUTH";
	}
}
