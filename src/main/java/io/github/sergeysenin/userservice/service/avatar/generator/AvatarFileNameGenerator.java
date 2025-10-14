package io.github.sergeysenin.userservice.service.avatar.generator;

import io.github.sergeysenin.userservice.config.avatar.AvatarProperties;
import io.github.sergeysenin.userservice.dto.avatar.AvatarFileIdsDto;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

/*
AvatarFileNameGenerator — формирует уникальные идентификаторы.
 */
@Component
@RequiredArgsConstructor
public class AvatarFileNameGenerator {

    private final AvatarProperties avatarProperties;

    public AvatarFileIdsDto generate(Long userId, String format) {
        // Реализация метода
        return null;
    }
}
