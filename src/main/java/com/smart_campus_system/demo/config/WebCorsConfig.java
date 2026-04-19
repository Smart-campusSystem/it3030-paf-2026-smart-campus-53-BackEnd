package com.smart_campus_system.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Allows the React (Vite) dev server to call the REST API from the browser.
 * Without this, {@code fetch()} fails with "Failed to fetch" due to CORS.
 */
@Configuration
public class WebCorsConfig {

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/api/**")
						// Vite often uses 5173; if busy it picks 5174, 5175, … — patterns cover all.
						.allowedOriginPatterns(
								"http://localhost:*",
								"http://127.0.0.1:*"
						)
						.allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")
						.allowedHeaders("*")
						.maxAge(3600)
						.allowCredentials(false);
			}
		};
	}
}
