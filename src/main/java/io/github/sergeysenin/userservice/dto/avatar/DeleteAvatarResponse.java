package io.github.sergeysenin.userservice.dto.avatar;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DeleteAvatarResponse(

        @JsonProperty("userId")
        Long userId,

        @JsonProperty("removed")
        boolean removed,

        @JsonProperty("files")
        AvatarObjectPathsDto removedFiles
) {
}
