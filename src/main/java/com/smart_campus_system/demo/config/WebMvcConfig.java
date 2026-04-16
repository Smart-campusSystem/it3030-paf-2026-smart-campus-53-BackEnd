package com.smart_campus_system.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	@Value("${app.profile-storage.local-dir:data/profile-uploads}")
	private String localProfileDir;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String location = Path.of(localProfileDir).toAbsolutePath().normalize().toUri().toString();
		if (!location.endsWith("/")) {
			location = location + "/";
		}
		registry.addResourceHandler("/uploads/profile/**").addResourceLocations(location);
	}
}
