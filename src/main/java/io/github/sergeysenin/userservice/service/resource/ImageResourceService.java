package io.github.sergeysenin.userservice.service.resource;

import io.github.sergeysenin.userservice.exception.type.AvatarUploadException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.coobird.thumbnailator.Thumbnails;

import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageResourceService implements ResourceService {

    @Override
    public byte[] resize(byte[] originalBytes, int maxSide, String format) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(originalBytes);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Thumbnails.of(inputStream)
                    .keepAspectRatio(true)
                    .size(maxSide, maxSide)
                    .outputFormat(format)
                    .toOutputStream(outputStream);

            byte[] result = outputStream.toByteArray();

            log.debug("Изменён размер изображения: maxSide={}, format={}, in={} bytes, out={} bytes",
                    maxSide, format, originalBytes.length, result.length);

            return result;

        } catch (IOException | RuntimeException exception) {
            log.error("Ошибка изменения размера изображения: maxSide={}, format={}, in={} bytes",
                    maxSide, format, originalBytes != null ? originalBytes.length : 0, exception);
            throw new AvatarUploadException("Не удалось изменить размер изображения", exception);
        }
    }
}
