package io.github.sergeysenin.userservice.dto.avatar;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Ответ на запрос текущего аватара пользователя.
 */
public record GetAvatarResponse(

        @JsonProperty("userId")
        Long userId,

        @JsonProperty("files")
        AvatarFileIdsDto fileIds,

        @JsonProperty("hasAvatar")
        boolean hasAvatar
) {
}
