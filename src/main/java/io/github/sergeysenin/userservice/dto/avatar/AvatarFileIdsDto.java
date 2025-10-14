package io.github.sergeysenin.userservice.dto.avatar;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO с идентификаторами (ключами) файлов аватара.
 */
public record AvatarFileIdsDto(

        @JsonProperty("original")
        String originalKey,

        @JsonProperty("profile")
        String profileKey,

        @JsonProperty("thumbnail")
        String thumbnailKey
) {
}
