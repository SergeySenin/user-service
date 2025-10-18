package io.github.sergeysenin.userservice.validator.resource;

import io.github.sergeysenin.userservice.config.avatar.AvatarProperties;
import io.github.sergeysenin.userservice.exception.type.DataValidationException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ResourceValidator {

    private final AvatarProperties avatarProperties;

    /*
     * TODO: для лёгкой расширяемости можно вынести соответствия MIME-типов и расширений
     *       в AvatarProperties и собирать их из конфигурации (например, application.yaml).
     *       Тогда добавление нового формата сведётся к настройке без правок кода.
     *
     *       Сейчас проект учебный, поэтому держим маппинги в коде, чтобы не усложнять
     *       инфраструктуру и не раздувать количество настроек. В боевом сервисе такую
     *       логику лучше вынести в конфигурацию, добавить валидацию данных при старте и
     *       покрыть автотестами, чтобы команда могла добавлять новые форматы без сборки.
     *       Текущее решение считается мало расширяемым, потому что любое расширение
     *       потребует правки исходников и повторного деплоя, а значит, затруднит поддержку.
     */

    private static final Map<String, String> EXTENSION_NORMALIZATION = Map.of(
            "jpeg", "jpg",
            "jpg", "jpg",
            "png", "png",
            "webp", "webp"
    );

    private static final Map<String, String> MIME_TYPE_TO_EXTENSION = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private static final Set<String> SUPPORTED_CANONICAL_EXTENSIONS = Set.copyOf(EXTENSION_NORMALIZATION.values());

    public String getValidatedExtension(MultipartFile file) {
        if (file == null) {
            throw new DataValidationException("Файл не найден");
        }

        if (file.isEmpty() || file.getSize() <= 0) {
            throw new DataValidationException("Файл пустой");
        }

        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new DataValidationException("Не указано имя файла");
        }

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType)) {
            throw new DataValidationException("Не удалось определить MIME-тип файла");
        }

        String normalizedContentType = contentType.trim().toLowerCase(Locale.ROOT);
        int slashIndex = normalizedContentType.indexOf('/');
        if (slashIndex <= 0 || slashIndex == normalizedContentType.length() - 1) {
            throw new DataValidationException("Некорректный MIME-тип файла");
        }

        if (!normalizedContentType.startsWith("image/")) {
            throw new DataValidationException("Ожидался MIME-тип изображения");
        }

        Set<String> allowedMimeTypes = Set.copyOf(avatarProperties.allowedMimeTypes());

        if (allowedMimeTypes.isEmpty() || !allowedMimeTypes.contains(normalizedContentType)) {
            throw new DataValidationException("Недопустимый MIME-тип файла");
        }

        String extension = StringUtils.getFilenameExtension(originalFilename);
        if (!StringUtils.hasText(extension)) {
            throw new DataValidationException("Не удалось определить расширение файла");
        }

        String normalizedExtension = extension.trim().toLowerCase(Locale.ROOT);
        String canonicalExtension = EXTENSION_NORMALIZATION.get(normalizedExtension);
        if (canonicalExtension == null || !SUPPORTED_CANONICAL_EXTENSIONS.contains(canonicalExtension)) {
            throw new DataValidationException("Неподдерживаемое расширение файла");
        }

        String mimeBasedExtension = Objects.requireNonNullElse(
                MIME_TYPE_TO_EXTENSION.get(normalizedContentType),
                normalizedContentType.substring(slashIndex + 1)
        );

        String canonicalMimeExtension = EXTENSION_NORMALIZATION.getOrDefault(mimeBasedExtension, mimeBasedExtension);

        if (!canonicalExtension.equals(canonicalMimeExtension)) {
            throw new DataValidationException("Расширение файла не соответствует MIME-типу");
        }

        return canonicalExtension;
    }
}
