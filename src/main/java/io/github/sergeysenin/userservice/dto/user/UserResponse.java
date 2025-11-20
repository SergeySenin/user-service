package io.github.sergeysenin.userservice.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record UserResponse(

        @JsonProperty("id")
        Long id,

        @JsonProperty("username")
        String username,

        @JsonProperty("email")
        String email,

        @JsonProperty("phone")
        String phone,

        @JsonProperty("active")
        boolean active,

        @JsonProperty("aboutMe")
        String aboutMe,

        @JsonProperty("country")
        CountrySummaryDto country,

        @JsonProperty("city")
        String city,

        @JsonProperty("experience")
        Short experience,

        @JsonProperty("hasAvatar")
        boolean hasAvatar,

        @JsonProperty("createdAt")
        OffsetDateTime createdAt,

        @JsonProperty("updatedAt")
        OffsetDateTime updatedAt
) {
}
