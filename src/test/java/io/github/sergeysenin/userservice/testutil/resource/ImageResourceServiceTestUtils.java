package io.github.sergeysenin.userservice.testutil.resource;

import net.coobird.thumbnailator.Thumbnails;

import java.io.InputStream;

import static org.mockito.Mockito.mock;

/**
 * Утильные методы для моков Thumbnailator в тестах {@code ImageResourceService}.
 */
public final class ImageResourceServiceTestUtils {

    private ImageResourceServiceTestUtils() {
    }

    // Создаёт мок билдера Thumbnailator для подмены поведения ресайзинга.
    @SuppressWarnings("unchecked")
    public static Thumbnails.Builder<InputStream> thumbnailBuilderMock() {
        return mock(Thumbnails.Builder.class);
    }
}
