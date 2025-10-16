package io.github.sergeysenin.userservice.dto.avatar;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GetAvatarResponse(

        @JsonProperty("userId")
        Long userId,

        @JsonProperty("files")
        AvatarObjectPathsDto fileIds,

        @JsonProperty("hasAvatar")
        boolean hasAvatar
) {
}
