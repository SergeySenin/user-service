package io.github.sergeysenin.userservice.mapper.avatar;

import io.github.sergeysenin.userservice.dto.avatar.AvatarObjectPathsDto;
import io.github.sergeysenin.userservice.entity.user.UserProfileAvatar;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface AvatarMapper {

    AvatarObjectPathsDto toDto(UserProfileAvatar avatar);

    UserProfileAvatar toEntity(AvatarObjectPathsDto paths);
}
