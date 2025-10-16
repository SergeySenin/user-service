package io.github.sergeysenin.userservice.dto.avatar;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record UploadAvatarResponse(

        @JsonProperty("userId")
        Long userId,

        @JsonProperty("files")
        AvatarObjectPathsDto fileIds,

        @JsonProperty("updatedAt")
        OffsetDateTime updatedAt
) {
}
