package com.smart_campus_system.demo.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

/**
 * S3 client and presigner for profile images. Uses the explicit credentials or AWS SDK default credential chain.
 */
@Configuration
public class S3ClientConfig {

	private AwsCredentialsProvider getCredentialsProvider(String accessKey, String secretKey) {
		if (accessKey != null && !accessKey.isEmpty() && secretKey != null && !secretKey.isEmpty()) {
			return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
		}
		return DefaultCredentialsProvider.create();
	}

	@Bean
	public S3Client s3Client(@Value("${app.profile-storage.s3.region}") String region,
							 @Value("${app.aws.access-key-id:}") String accessKey,
							 @Value("${app.aws.secret-access-key:}") String secretKey) {
		return S3Client.builder()
				.region(Region.of(region))
				.credentialsProvider(getCredentialsProvider(accessKey, secretKey))
				.build();
	}

	@Bean
	public S3Presigner s3Presigner(@Value("${app.profile-storage.s3.region}") String region,
								   @Value("${app.aws.access-key-id:}") String accessKey,
								   @Value("${app.aws.secret-access-key:}") String secretKey) {
		return S3Presigner.builder()
				.region(Region.of(region))
				.credentialsProvider(getCredentialsProvider(accessKey, secretKey))
				.build();
	}
}
