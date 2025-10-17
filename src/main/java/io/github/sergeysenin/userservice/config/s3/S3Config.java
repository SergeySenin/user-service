package io.github.sergeysenin.userservice.config.s3;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class S3Config {

    @Bean
    public S3Configuration s3Configuration() {
        return S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();
    }

    @Bean(destroyMethod = "close")
    public S3Client s3Client(S3Properties properties, S3Configuration configuration) {
        Region region = resolveRegion(properties);

        return S3Client.builder()
                .endpointOverride(URI.create(properties.endpoint()))
                .region(region)
                .credentialsProvider(credentialsProvider(properties))
                .serviceConfiguration(configuration)
                .build();
    }

    @Bean(destroyMethod = "close")
    public S3Presigner s3Presigner(S3Properties properties, S3Configuration configuration) {
        Region region = resolveRegion(properties);

        return S3Presigner.builder()
                .endpointOverride(URI.create(properties.endpoint()))
                .region(region)
                .credentialsProvider(credentialsProvider(properties))
                .serviceConfiguration(configuration)
                .build();
    }

    private StaticCredentialsProvider credentialsProvider(S3Properties properties) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(properties.accessKey(), properties.secretKey());
        return StaticCredentialsProvider.create(credentials);
    }

    private Region resolveRegion(S3Properties properties) {
        String region = properties.region();
        if (!StringUtils.hasText(region)) {
            throw new IllegalStateException("Регион S3 не должен быть пустым");
        }

        return Region.of(region.trim());
    }
}
