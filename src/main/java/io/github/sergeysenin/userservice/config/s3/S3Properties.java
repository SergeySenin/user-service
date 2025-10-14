package io.github.sergeysenin.userservice.config.s3;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * {@link S3Properties} — типобезопасная конфигурация подключения к Minio/S3.
 */
@Validated
@ConfigurationProperties(prefix = "services.s3")
public record S3Properties(

        @NotBlank(message = "Endpoint хранилища не может быть пустым")
        String endpoint,

        @NotBlank(message = "Access key не может быть пустым")
        String accessKey,

        @NotBlank(message = "Secret key не может быть пустым")
        String secretKey,

        @NotBlank(message = "Название бакета не может быть пустым")
        String bucketName,

        @NotBlank(message = "Регион не может быть пустым")
        String region,

        @NotNull(message = "Время жизни presigned URL должно быть задано")
        @DurationUnit(ChronoUnit.MILLIS)
        Duration urlExpiration
) {

    public S3Properties(
            String endpoint,
            String accessKey,
            String secretKey,
            String bucketName,
            String region,
            @DefaultValue("PT15M") Duration urlExpiration
    ) {
        this.endpoint = requireNonBlank(endpoint, "endpoint");
        this.accessKey = requireNonBlank(accessKey, "accessKey");
        this.secretKey = requireNonBlank(secretKey, "secretKey");
        this.bucketName = requireNonBlank(bucketName, "bucketName");
        this.region = requireNonBlank(region, "region");
        this.urlExpiration = Objects.requireNonNull(urlExpiration, "urlExpiration не может быть null");
    }

    private static String requireNonBlank(String value, String fieldName) {
        String trimmed = Objects.requireNonNull(value, fieldName + " не может быть null").trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " не может быть пустым");
        }
        return trimmed;
    }
}
