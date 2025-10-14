package io.github.sergeysenin.userservice.service.resource;

import io.github.sergeysenin.userservice.exception.type.AvatarUploadException;

import lombok.extern.slf4j.Slf4j;

import net.coobird.thumbnailator.Thumbnails;

import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

/**
 * Реализация {@link ResourceService}, выполняющая ресайз изображений при помощи Thumbnailator.
 */
@Slf4j
@Service
public class ImageResourceService implements ResourceService {

    @Override
    public byte[] resize(byte[] originalBytes, int maxSide, String format) {
        Objects.requireNonNull(originalBytes, "originalBytes не может быть null");
        if (originalBytes.length == 0) {
            throw new AvatarUploadException("Исходное изображение не содержит данных");
        }
        if (maxSide <= 0) {
            throw new IllegalArgumentException("maxSide должен быть положительным");
        }
        String normalizedFormat = normalizeFormat(format);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(originalBytes);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Thumbnails.of(inputStream)
                    .size(maxSide, maxSide)
                    .keepAspectRatio(true)
                    .outputFormat(normalizedFormat)
                    .toOutputStream(outputStream);

            return outputStream.toByteArray();
        } catch (IOException ex) {
            log.error("Ошибка при ресайзе изображения", ex);
            throw new AvatarUploadException("Не удалось изменить размер изображения", ex);
        }
    }

    private String normalizeFormat(String format) {
        String value = Objects.requireNonNull(format, "format не может быть null").trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("format не может быть пустым");
        }
        String normalized = value.startsWith(".") ? value.substring(1) : value;
        return normalized.toLowerCase(Locale.ROOT);
    }
}
