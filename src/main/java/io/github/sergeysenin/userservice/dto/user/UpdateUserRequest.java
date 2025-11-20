package io.github.sergeysenin.userservice.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        @JsonProperty("username")
        @Size(max = 64, message = "Логин должен содержать не более 64 символов")
        String username,

        @JsonProperty("email")
        @Email(message = "Email имеет некорректный формат")
        @Size(max = 256, message = "Email должен содержать не более 256 символов")
        String email,

        @JsonProperty("phone")
        @Size(max = 16, message = "Телефон должен содержать не более 16 символов")
        String phone,

        @JsonProperty("active")
        Boolean active,

        @JsonProperty("aboutMe")
        @Size(max = 2048, message = "Раздел 'О себе' должен содержать не более 2048 символов")
        String aboutMe,

        @JsonProperty("countryId")
        Long countryId,

        @JsonProperty("city")
        @Size(max = 64, message = "Город должен содержать не более 64 символов")
        String city,

        @JsonProperty("experience")
        @Min(value = 0, message = "Опыт не может быть отрицательным")
        @Max(value = 32767, message = "Опыт превышает допустимое значение")
        Short experience
) {
}
