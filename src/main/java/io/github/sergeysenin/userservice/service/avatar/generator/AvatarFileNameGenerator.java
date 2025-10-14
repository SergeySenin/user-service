package io.github.sergeysenin.userservice.service.avatar.generator;

import io.github.sergeysenin.userservice.config.avatar.AvatarProperties;
import io.github.sergeysenin.userservice.dto.avatar.AvatarFileIdsDto;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/*
AvatarFileNameGenerator — формирует уникальные идентификаторы.
 */
@Component
@RequiredArgsConstructor
public class AvatarFileNameGenerator {

    private final AvatarProperties avatarProperties;

    public AvatarFileIdsDto generate(Long userId, String format) {
        Objects.requireNonNull(userId, "userId не может быть null");
        String sanitizedFormat = sanitizeFormat(format);

        String basePath = avatarProperties.storagePath();
        String userFolder = userId + "/" + UUID.randomUUID();
        String prefix = basePath + "/" + userFolder;

        String originalKey = prefix + "/original." + sanitizedFormat;
        String profileKey = prefix + "/profile." + sanitizedFormat;
        String thumbnailKey = prefix + "/thumbnail." + sanitizedFormat;

        return new AvatarFileIdsDto(originalKey, profileKey, thumbnailKey);
    }

    private String sanitizeFormat(String format) {
        String value = Objects.requireNonNull(format, "format не может быть null").trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("format не может быть пустым");
        }
        String normalized = value.startsWith(".") ? value.substring(1) : value;
        return normalized.toLowerCase(Locale.ROOT);
    }
}
