package com.smart_campus_system.demo.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConditionalOnProperty(name = "app.profile-storage.mode", havingValue = "s3")
public class S3ClientConfig {

	@Bean
	public S3Client s3Client(
			@Value("${app.profile-storage.s3.region:us-east-1}") String region,
			@Value("${app.profile-storage.s3.access-key:}") String accessKey,
			@Value("${app.profile-storage.s3.secret-key:}") String secretKey) {
		var builder = S3Client.builder().region(Region.of(region));
		if (accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank()) {
			builder.credentialsProvider(
					StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)));
		} else {
			builder.credentialsProvider(DefaultCredentialsProvider.create());
		}
		return builder.build();
	}
}
