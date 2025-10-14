package io.github.sergeysenin.userservice.dto.avatar;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/**
 * Ответ на успешную загрузку аватара.
 */
public record UploadAvatarResponse(

        @JsonProperty("userId")
        Long userId,

        @JsonProperty("files")
        AvatarFileIdsDto fileIds,

        @JsonProperty("updatedAt")
        OffsetDateTime updatedAt
) {
}
