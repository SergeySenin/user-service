package io.github.sergeysenin.userservice.config.s3;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

/*
S3Config — создаёт бины S3Client и S3Presigner с path-style и статическими учётными данными,
используя значения из S3Properties; закрывает их по завершении работы.

Зависит от S3Properties (endpoint, ключи, регион).

s3Client(), s3Presigner() конфигурируют клиентов AWS SDK v2.
 */
@Configuration
public class S3Config {

    @Bean(destroyMethod = "close")
    public S3Client s3Client(S3Properties properties) {
        return S3Client.builder()
                .endpointOverride(URI.create(properties.endpoint()))
                .region(Region.of(properties.region()))
                .credentialsProvider(StaticCredentialsProvider.create(buildCredentials(properties)))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }

    @Bean(destroyMethod = "close")
    public S3Presigner s3Presigner(S3Properties properties) {
        return S3Presigner.builder()
                .endpointOverride(URI.create(properties.endpoint()))
                .region(Region.of(properties.region()))
                .credentialsProvider(StaticCredentialsProvider.create(buildCredentials(properties)))
                .build();
    }

    private AwsBasicCredentials buildCredentials(S3Properties properties) {
        return AwsBasicCredentials.create(properties.accessKey(), properties.secretKey());
    }
}
