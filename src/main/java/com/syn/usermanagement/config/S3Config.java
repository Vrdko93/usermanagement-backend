package com.syn.usermanagement.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
@Configuration
public class S3Config {
    @Bean S3Client s3Client() { return S3Client.create(); }
    @Bean S3Presigner s3Presigner() { return S3Presigner.create(); }
}
