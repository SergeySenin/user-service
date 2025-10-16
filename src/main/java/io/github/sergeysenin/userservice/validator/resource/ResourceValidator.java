package io.github.sergeysenin.userservice.validator.resource;

import io.github.sergeysenin.userservice.config.avatar.AvatarProperties;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResourceValidator {

    private final AvatarProperties avatarProperties;


}

// Написать:
// public String getValidatedExtension(MultipartFile file) {
// Проверка существования файла
// Проверка пустоты файла
// Проверка размера файла; уже есть в src/main/resources/application.yaml:
//     multipart:
//      max-file-size: 5MB
//      max-request-size: 5MB
// Проверка имени файла
// Проверка MIME-типа на наличие
// Проверка префикса MIME-типа
// Проверка разрешенных MIME-типов
// Проверка расширения файла
// Проверка поддерживаемых форматов по расширению
// Определение расширения из MIME-типа
// Сравнение расширений
// }
