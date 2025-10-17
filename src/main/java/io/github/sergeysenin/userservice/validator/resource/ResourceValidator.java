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

        Set<String> allowedMimeTypes = avatarProperties.allowedMimeTypes() == null ?
                Set.of() :
                avatarProperties.allowedMimeTypes().stream()
                .filter(Objects::nonNull)
                .map(mime -> mime.trim().toLowerCase(Locale.ROOT))
                .filter(mime -> !mime.isEmpty())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());

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
