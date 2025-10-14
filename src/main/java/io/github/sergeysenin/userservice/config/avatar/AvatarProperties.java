package io.github.sergeysenin.userservice.config.avatar;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * {@link AvatarProperties} — типобезопасные настройки модуля аватаров.
 * <p>
 * Значения подставляются из {@code application.yaml} и профильных конфигураций.
 */
@Validated
@ConfigurationProperties(prefix = "user.avatar")
public record AvatarProperties(

        @NotBlank(message = "Базовый путь хранения аватаров не может быть пустым")
        String storagePath,

        @NotNull(message = "Настройки размеров аватара должны быть заданы")
        @Valid Sizes sizes,

        @NotEmpty(message = "Список допустимых MIME-типов не может быть пустым")
        List<@NotBlank(message = "MIME-тип не может быть пустым") String> allowedMimeTypes
) {

    public AvatarProperties(
            String storagePath,
            Sizes sizes,
            @DefaultValue("image/jpeg,image/png,image/webp") List<String> allowedMimeTypes
    ) {
        this.storagePath = normalizeStoragePath(storagePath);
        this.sizes = Objects.requireNonNull(sizes, "sizes не может быть null");
        this.allowedMimeTypes = sanitizeMimeTypes(allowedMimeTypes);
    }

    private static String normalizeStoragePath(String storagePath) {
        String value = Objects.requireNonNull(storagePath, "storagePath не может быть null").trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("storagePath не может быть пустым");
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private static List<String> sanitizeMimeTypes(List<String> mimeTypes) {
        Objects.requireNonNull(mimeTypes, "allowedMimeTypes не может быть null");
        return mimeTypes.stream()
                .map(value -> {
                    String trimmed = Objects.requireNonNull(value, "MIME-тип не может быть null").trim();
                    if (trimmed.isEmpty()) {
                        throw new IllegalArgumentException("MIME-тип не может быть пустым");
                    }
                    return trimmed.toLowerCase(Locale.ROOT);
                })
                .distinct()
                .toList();
    }

    public record Sizes(

            @NotNull(message = "Размер thumbnail должен быть задан")
            @Valid AvatarSizeProperties thumbnail,

            @NotNull(message = "Размер profile должен быть задан")
            @Valid AvatarSizeProperties profile
    ) {

        public Sizes(AvatarSizeProperties thumbnail, AvatarSizeProperties profile) {
            this.thumbnail = Objects.requireNonNull(thumbnail, "thumbnail не может быть null");
            this.profile = Objects.requireNonNull(profile, "profile не может быть null");
        }
    }

    public record AvatarSizeProperties(

            @Positive(message = "Максимальная сторона аватара должна быть положительной")
            int maxSide
    ) {

        public AvatarSizeProperties(int maxSide) {
            if (maxSide <= 0) {
                throw new IllegalArgumentException("maxSide должен быть положительным");
            }
            this.maxSide = maxSide;
        }
    }
}
