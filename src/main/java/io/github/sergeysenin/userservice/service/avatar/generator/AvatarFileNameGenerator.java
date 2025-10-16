package io.github.sergeysenin.userservice.service.avatar.generator;

import io.github.sergeysenin.userservice.config.avatar.AvatarProperties;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AvatarFileNameGenerator {

    private final AvatarProperties avatarProperties;


}

// Написать:
// public AvatarObjectPathsDto generateFilePaths(Long userId, String fileExtension) {
// Генерация структуры путей
// Создание путей для разных версий
// }
// В реализации:
// Storage Path (базовый путь из конфигурации)/
// User ID (ID пользователя)/
// UUID (уникальный идентификатор аватара)/
// Тип версии (original/thumbnail/profile)/
// Расширение файла (из MIME-типа)
// avatars/12345/550e8400-e29b-41d4-a716-446655440000/original.jpg
