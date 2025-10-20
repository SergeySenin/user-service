package io.github.sergeysenin.userservice.service.resource;

import io.github.sergeysenin.userservice.exception.type.AvatarUploadException;

import net.coobird.thumbnailator.Thumbnails;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static io.github.sergeysenin.userservice.testutil.resource.ImageResourceServiceTestUtils.thumbnailBuilderMock;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DisplayName("ImageResourceService")
class ImageResourceServiceTest {

    private static final byte[] ORIGINAL_BYTES = {10, 20, 30};
    private static final byte[] RESIZED_BYTES = {1, 2};
    private static final int MAX_SIDE = 256;
    private static final String FORMAT = "png";
    private static final String ERROR_MESSAGE = "Не удалось изменить размер изображения";

    private ImageResourceService createSut() {
        return new ImageResourceService();
    }

    @Nested
    @DisplayName("resize")
    class Resize {

        @Test
        @DisplayName("должен возвращать изменённое изображение, когда Thumbnailator работает корректно")
        void shouldReturnResizedBytesWhenThumbnailatorProcessesInput() throws IOException {
            final var sut = createSut();

            try (MockedStatic<Thumbnails> thumbnails = Mockito.mockStatic(Thumbnails.class)) {
                final var builder = thumbnailBuilderMock();
                thumbnails.when(() -> Thumbnails.of(any(InputStream.class))).thenReturn(builder);
                when(builder.size(MAX_SIDE, MAX_SIDE)).thenReturn(builder);
                when(builder.outputFormat(FORMAT)).thenReturn(builder);
                doAnswer(invocation -> {
                    final OutputStream outputStream = invocation.getArgument(0);
                    outputStream.write(RESIZED_BYTES);
                    return null;
                }).when(builder).toOutputStream(any(OutputStream.class));

                final var result = sut.resize(ORIGINAL_BYTES, MAX_SIDE, FORMAT);

                assertArrayEquals(RESIZED_BYTES, result,
                        "Метод должен возвращать байты, записанные Thumbnailator");

                verify(builder).size(MAX_SIDE, MAX_SIDE);
                verify(builder).outputFormat(FORMAT);
                verify(builder).toOutputStream(any(OutputStream.class));
                verifyNoMoreInteractions(builder);
                thumbnails.verify(() -> Thumbnails.of(any(InputStream.class)));
                thumbnails.verifyNoMoreInteractions();
            }
        }

        @Test
        @DisplayName("должен бросать AvatarUploadException, когда происходит IOException")
        void shouldThrowAvatarUploadExceptionWhenThumbnailatorThrowsIoexception() throws IOException {
            final var sut = createSut();

            try (MockedStatic<Thumbnails> thumbnails = Mockito.mockStatic(Thumbnails.class)) {
                final var builder = thumbnailBuilderMock();
                thumbnails.when(() -> Thumbnails.of(any(InputStream.class))).thenReturn(builder);
                when(builder.size(MAX_SIDE, MAX_SIDE)).thenReturn(builder);
                when(builder.outputFormat(FORMAT)).thenReturn(builder);
                final var ioException = new IOException("write failed");
                doThrow(ioException).when(builder).toOutputStream(any(OutputStream.class));

                final var exception = assertThrows(
                        AvatarUploadException.class,
                        () -> sut.resize(ORIGINAL_BYTES, MAX_SIDE, FORMAT),
                        "Ожидалось исключение при ошибке Thumbnailator"
                );

                assertAll("Проверка данных исключения Thumbnailator",
                        () -> assertEquals(ERROR_MESSAGE, exception.getMessage(),
                                "Сообщение должно указывать на ошибку изменения размера"),
                        () -> assertEquals(ioException, exception.getCause(),
                                "Причина должна содержать исходный IOException")
                );

                verify(builder).size(MAX_SIDE, MAX_SIDE);
                verify(builder).outputFormat(FORMAT);
                verify(builder).toOutputStream(any(OutputStream.class));
                verifyNoMoreInteractions(builder);
                thumbnails.verify(() -> Thumbnails.of(any(InputStream.class)));
                thumbnails.verifyNoMoreInteractions();
            }
        }

        @Test
        @DisplayName("должен бросать AvatarUploadException, когда Thumbnailator выбрасывает RuntimeException")
        void shouldThrowAvatarUploadExceptionWhenThumbnailatorThrowsRuntimeException() {
            final var sut = createSut();
            final var runtimeException = new IllegalStateException("builder failed");

            try (MockedStatic<Thumbnails> thumbnails = Mockito.mockStatic(Thumbnails.class)) {
                thumbnails.when(() -> Thumbnails.of(any(InputStream.class))).thenThrow(runtimeException);

                final var exception = assertThrows(
                        AvatarUploadException.class,
                        () -> sut.resize(ORIGINAL_BYTES, MAX_SIDE, FORMAT),
                        "Ожидалось исключение при ошибке Thumbnailator"
                );

                assertAll("Проверка исключения при runtime-ошибке Thumbnailator",
                        () -> assertEquals(ERROR_MESSAGE, exception.getMessage(),
                                "Сообщение должно указывать на ошибку изменения размера"),
                        () -> assertEquals(runtimeException, exception.getCause(),
                                "Причина должна содержать исходное исключение")
                );

                thumbnails.verify(() -> Thumbnails.of(any(InputStream.class)));
                thumbnails.verifyNoMoreInteractions();
            }
        }
    }
}
