package io.github.sergeysenin.userservice.mapper.avatar.impl;

import io.github.sergeysenin.userservice.dto.avatar.AvatarObjectPathsDto;
import io.github.sergeysenin.userservice.entity.user.UserProfileAvatar;
import io.github.sergeysenin.userservice.mapper.avatar.AvatarMapper;

import org.springframework.stereotype.Component;

@Component
public class ManualAvatarMapper implements AvatarMapper {

    @Override
    public AvatarObjectPathsDto toDto(UserProfileAvatar avatar) {
        if (avatar == null) {
            return null;
        }

        return new AvatarObjectPathsDto(
                avatar.getOriginalPath(),
                avatar.getThumbnailPath(),
                avatar.getProfilePath()
        );
    }

    @Override
    public UserProfileAvatar toEntity(AvatarObjectPathsDto paths) {
        if (paths == null) {
            return null;
        }

        return UserProfileAvatar.builder()
                .originalPath(paths.originalPath())
                .thumbnailPath(paths.thumbnailPath())
                .profilePath(paths.profilePath())
                .build();
    }
}
