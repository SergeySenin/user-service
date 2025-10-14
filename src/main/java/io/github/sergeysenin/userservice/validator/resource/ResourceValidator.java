package io.github.sergeysenin.userservice.validator.resource;

import io.github.sergeysenin.userservice.config.avatar.AvatarProperties;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

/*
ResourceValidator — проверяет, что файл передан, не пустой, укладывается в лимит,
имеет корректный префикс и расширение из поддерживаемого списка, используя AvatarProperties.
 */
@Component
@RequiredArgsConstructor
public class ResourceValidator {

    private final AvatarProperties avatarProperties;

    // Необходимая валидация
}
