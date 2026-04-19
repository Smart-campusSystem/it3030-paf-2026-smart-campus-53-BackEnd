package com.smart_campus_system.demo.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * S3 client and presigner for profile images. Uses the AWS SDK default credential chain.
 */
@Configuration
public class S3ClientConfig {

	@Bean
	public S3Client s3Client(@Value("${app.profile-storage.s3.region}") String region) {
		return S3Client.builder()
				.region(Region.of(region))
				.credentialsProvider(DefaultCredentialsProvider.create())
				.build();
	}

	@Bean
	public S3Presigner s3Presigner(@Value("${app.profile-storage.s3.region}") String region) {
		return S3Presigner.builder()
				.region(Region.of(region))
				.credentialsProvider(DefaultCredentialsProvider.create())
				.build();
	}
}
