package io.github.sergeysenin.userservice.entity.user;

import jakarta.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class UserProfileAvatar {

    private String originalPath;

    private String thumbnailPath;

    private String profilePath;
}
