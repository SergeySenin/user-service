package io.github.sergeysenin.userservice.validator.resource;

import io.github.sergeysenin.userservice.config.avatar.AvatarProperties;
import io.github.sergeysenin.userservice.exception.type.DataValidationException;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Валидатор входящего файла для загрузки аватара.
 */
@Component
@RequiredArgsConstructor
public class ResourceValidator {

    private final AvatarProperties avatarProperties;

    /**
     * Проверяет входной файл и возвращает допустимое расширение.
     *
     * @param file загружаемый файл
     * @return расширение файла без точки
     */
    public String validateAndExtractExtension(MultipartFile file) {
        if (file == null) {
            throw new DataValidationException("Файл не передан");
        }
        if (file.isEmpty() || file.getSize() <= 0) {
            throw new DataValidationException("Файл не содержит данных");
        }

        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            throw new DataValidationException("Не удалось определить MIME-тип файла");
        }

        List<String> allowedMimeTypes = avatarProperties.allowedMimeTypes();
        boolean mimeAllowed = allowedMimeTypes.stream()
                .anyMatch(allowed -> allowed.equalsIgnoreCase(contentType));

        if (!mimeAllowed) {
            throw new DataValidationException("Недопустимый MIME-тип файла: " + contentType);
        }

        return resolveExtension(contentType);
    }

    private String resolveExtension(String contentType) {
        String subtype = MediaType.parseMediaType(contentType).getSubtype();
        if (subtype == null || subtype.isBlank()) {
            throw new DataValidationException("Не удалось определить расширение файла");
        }

        String normalized = subtype.toLowerCase(Locale.ROOT);
        if (Objects.equals(normalized, "jpeg")) {
            return "jpg";
        }
        return normalized;
    }
}
