package io.github.sergeysenin.userservice.service.avatar.generator;

import io.github.sergeysenin.userservice.config.avatar.AvatarProperties;
import io.github.sergeysenin.userservice.dto.avatar.AvatarObjectPathsDto;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AvatarFileNameGenerator {

    private static final String ORIGINAL_VERSION = "original";
    private static final String THUMBNAIL_VERSION = "thumbnail";
    private static final String PROFILE_VERSION = "profile";

    private final AvatarProperties avatarProperties;

    public AvatarObjectPathsDto generateFilePaths(Long userId, String fileExtension) {
        String storagePath = avatarProperties.storagePath();
        String avatarIdentifier = UUID.randomUUID().toString();

        String originalPath = buildPath(storagePath, userId, avatarIdentifier, ORIGINAL_VERSION, fileExtension);
        String thumbnailPath = buildPath(storagePath, userId, avatarIdentifier, THUMBNAIL_VERSION, fileExtension);
        String profilePath = buildPath(storagePath, userId, avatarIdentifier, PROFILE_VERSION, fileExtension);

        return new AvatarObjectPathsDto(originalPath, thumbnailPath, profilePath);
    }

    private String buildPath(
            String storagePath,
            Long userId,
            String avatarIdentifier,
            String version, String extension
    ) {
        return String.join(
                "/",
                storagePath,
                String.valueOf(userId),
                avatarIdentifier,
                version + "." + extension
        );
    }
}
