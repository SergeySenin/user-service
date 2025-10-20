package io.github.sergeysenin.userservice.testutil.avatar;

import io.github.sergeysenin.userservice.dto.avatar.AvatarObjectPathsDto;
import io.github.sergeysenin.userservice.entity.user.UserProfileAvatar;

/**
 * Утильные методы для подготовки тестовых данных аватара пользователя.
 */
public final class AvatarTestFactory {

    private AvatarTestFactory() {
    }

    // Создаёт сущность аватара на основе DTO с путями к файлам.
    public static UserProfileAvatar createAvatarEntity(AvatarObjectPathsDto paths) {
        return UserProfileAvatar.builder()
                .originalPath(paths.originalPath())
                .thumbnailPath(paths.thumbnailPath())
                .profilePath(paths.profilePath())
                .build();
    }
}
