package io.github.sergeysenin.userservice.config.avatar;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Validated
@ConfigurationProperties(prefix = "user.avatar")
public record AvatarProperties(

        String storagePath,

        @Valid
        AvatarSizesProperties sizes,

        List<String> allowedMimeTypes
) {

    public static final String DEFAULT_STORAGE_PATH = "avatars";
    public static final String MIME_TYPE_JPEG = "image/jpeg";
    public static final String MIME_TYPE_PNG = "image/png";
    public static final String MIME_TYPE_WEBP = "image/webp";
    public static final List<String> DEFAULT_ALLOWED_MIME_TYPES = List.of(
            MIME_TYPE_JPEG,
            MIME_TYPE_PNG,
            MIME_TYPE_WEBP
    );

    public AvatarProperties(

            @DefaultValue(DEFAULT_STORAGE_PATH)
            String storagePath,

            AvatarSizesProperties sizes,

            @DefaultValue({MIME_TYPE_JPEG, MIME_TYPE_PNG, MIME_TYPE_WEBP})
            List<String> allowedMimeTypes
    ) {
        this.storagePath = normalizeStoragePath(storagePath);
        this.sizes = sizes == null ? new AvatarSizesProperties(null, null) : sizes;
        this.allowedMimeTypes = normalizeAllowedMimeTypes(allowedMimeTypes);
    }

    public record AvatarSizesProperties(

            AvatarSizeProperties thumbnail,
            AvatarSizeProperties profile
    ) {

        public AvatarSizesProperties(

                AvatarSizeProperties thumbnail,
                AvatarSizeProperties profile
        ) {
            this.thumbnail = thumbnail == null ? new AvatarSizeProperties(170) : thumbnail;
            this.profile = profile == null ? new AvatarSizeProperties(1080) : profile;
        }
    }

    public record AvatarSizeProperties(

            @Positive
            int maxSide
    ) {

        public AvatarSizeProperties(int maxSide) {
            this.maxSide = maxSide;
        }
    }

    private static String normalizeStoragePath(String storagePath) {
        if (storagePath == null) {
            return DEFAULT_STORAGE_PATH;
        }

        String trimmed = storagePath.trim();
        String withoutSlashes = trimmed.replaceAll("^/+|/+$", "");
        return withoutSlashes.isEmpty() ? DEFAULT_STORAGE_PATH : withoutSlashes;
    }

    private static List<String> normalizeAllowedMimeTypes(List<String> allowedMimeTypes) {
        List<String> initial = allowedMimeTypes == null ? DEFAULT_ALLOWED_MIME_TYPES : allowedMimeTypes;

        Set<String> sanitized = initial.stream()
                .filter(Objects::nonNull)
                .map(mime -> mime.trim().toLowerCase(Locale.ROOT))
                .filter(mime -> !mime.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (sanitized.isEmpty()) {
            throw new IllegalArgumentException("Список допустимых MIME-типов аватара не может быть пустым");
        }

        return List.copyOf(sanitized);
    }
}
