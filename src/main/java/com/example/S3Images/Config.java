package com.example.S3Images;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.amazonaws.regions.Regions.US_EAST_1;

@Configuration
public class Config {

    @Bean
    public AmazonS3 s3Client() {
        return AmazonS3Client.builder()
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .withRegion(US_EAST_1)
                .build();
    }
}
