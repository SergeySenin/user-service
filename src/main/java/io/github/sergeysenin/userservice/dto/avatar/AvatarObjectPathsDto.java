package io.github.sergeysenin.userservice.dto.avatar;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AvatarObjectPathsDto(

        @JsonProperty("original")
        String originalPath,

        @JsonProperty("thumbnail")
        String thumbnailPath,

        @JsonProperty("profile")
        String profilePath
) {
}
