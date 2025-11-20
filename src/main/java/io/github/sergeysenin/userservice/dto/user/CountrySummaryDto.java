package io.github.sergeysenin.userservice.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CountrySummaryDto(

        @JsonProperty("id")
        Long id,

        @JsonProperty("title")
        String title
) {
}
