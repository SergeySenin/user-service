package io.github.sergeysenin.userservice.config.s3;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

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

    public S3Properties(

            String endpoint,

            String accessKey,

            String secretKey,

            String bucketName,

            @DefaultValue("us-east-1")
            String region,

            @DefaultValue("PT120H")
            Duration urlExpiration
    ) {
        this.endpoint = endpoint;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucketName = bucketName;
        this.region = region;
        this.urlExpiration = urlExpiration;
    }
}
