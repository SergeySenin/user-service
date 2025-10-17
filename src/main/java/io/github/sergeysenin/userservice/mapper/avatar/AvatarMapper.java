package io.github.sergeysenin.userservice.mapper.avatar;

import io.github.sergeysenin.userservice.dto.avatar.AvatarObjectPathsDto;
import io.github.sergeysenin.userservice.entity.user.UserProfileAvatar;

public interface AvatarMapper {

    AvatarObjectPathsDto toDto(UserProfileAvatar avatar);

    UserProfileAvatar toEntity(AvatarObjectPathsDto paths);
}
