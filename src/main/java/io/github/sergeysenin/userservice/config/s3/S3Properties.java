package io.github.sergeysenin.userservice.config.s3;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.Locale;

@Validated
@ConfigurationProperties(prefix = "services.s3")
public record S3Properties(

        @NotBlank
        String endpoint,

        @NotBlank
        String accessKey,

        @NotBlank
        String secretKey,

        @NotBlank
        String bucketName,

        String region,

        @NotNull
        Duration urlExpiration
) {

    public static final String DEFAULT_REGION = "us-east-1";

    public S3Properties(

            String endpoint,

            String accessKey,

            String secretKey,

            String bucketName,

            @DefaultValue(DEFAULT_REGION)
            String region,

            @DefaultValue("PT120H")
            Duration urlExpiration
    ) {
        this.endpoint = endpoint;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucketName = bucketName;
        this.region = normalizeRegion(region);
        this.urlExpiration = urlExpiration;
    }

    private static String normalizeRegion(String region) {
        if (region == null) {
            return DEFAULT_REGION;
        }

        String trimmed = region.trim();
        if (trimmed.isEmpty()) {
            return DEFAULT_REGION;
        }

        return trimmed.toLowerCase(Locale.ROOT);
    }
}
