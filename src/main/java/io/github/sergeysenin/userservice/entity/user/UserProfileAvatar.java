package io.github.sergeysenin.userservice.entity.user;

import jakarta.persistence.Embeddable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PROTECTED)
@Builder
public class UserProfileAvatar implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String originalPath;
    private String thumbnailPath;
    private String profilePath;
}
