package io.github.sergeysenin.userservice.validator.resource;

import io.github.sergeysenin.userservice.config.avatar.AvatarProperties;
import io.github.sergeysenin.userservice.exception.type.DataValidationException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DisplayName("ResourceValidator")
class ResourceValidatorTest {

    private static final String FILE_NOT_FOUND_MESSAGE = "Файл не найден";
    private static final String FILE_EMPTY_MESSAGE = "Файл пустой";
    private static final String FILENAME_MISSING_MESSAGE = "Не указано имя файла";
    private static final String MIME_UNDETERMINED_MESSAGE = "Не удалось определить MIME-тип файла";
    private static final String MIME_INVALID_MESSAGE = "Некорректный MIME-тип файла";
    private static final String MIME_EXPECTED_IMAGE_MESSAGE = "Ожидался MIME-тип изображения";
    private static final String MIME_NOT_ALLOWED_MESSAGE = "Недопустимый MIME-тип файла";
    private static final String EXTENSION_UNDETERMINED_MESSAGE = "Не удалось определить расширение файла";
    private static final String EXTENSION_UNSUPPORTED_MESSAGE = "Неподдерживаемое расширение файла";
    private static final String EXTENSION_MIME_MISMATCH_MESSAGE = "Расширение файла не соответствует MIME-типу";
    private static final String DEFAULT_FILE_PARAMETER = "avatar";
    private static final String DEFAULT_ORIGINAL_FILENAME = "photo.jpeg";
    private static final String DEFAULT_CONTENT_TYPE = AvatarProperties.MIME_TYPE_JPEG;
    private static final byte[] DEFAULT_FILE_CONTENT = {1, 2, 3};
    private static final long DEFAULT_FILE_SIZE = DEFAULT_FILE_CONTENT.length;
    private static final List<String> DEFAULT_ALLOWED_MIME_TYPES = List.of(
            AvatarProperties.MIME_TYPE_JPEG,
            AvatarProperties.MIME_TYPE_PNG,
            AvatarProperties.MIME_TYPE_WEBP
    );

    @Mock
    private AvatarProperties avatarProperties;

    private ResourceValidator createSut() {
        return new ResourceValidator(avatarProperties);
    }

    @Nested
    @DisplayName("Проверка наличия файла")
    class FileValidation {

        @Test
        @DisplayName("должен бросать исключение, когда файл не передан")
        void shouldThrowFileNotFoundWhenFileIsNull() {
            DataValidationException exception = assertThrows(
                    DataValidationException.class,
                    () -> createSut().getValidatedExtension(null),
                    "Ожидалось исключение, если файл отсутствует"
            );

            assertAll(
                    () -> assertEquals(FILE_NOT_FOUND_MESSAGE, exception.getMessage(),
                            "Сообщение должно указывать, что файл не найден"),
                    () -> verifyNoInteractionsWithAvatarProperties()
            );
        }

        @Test
        @DisplayName("должен бросать исключение, когда файл помечен как пустой")
        void shouldThrowFileEmptyWhenMultipartFileIsEmpty() {
            MultipartFile file = multipartFileBuilder()
                    .withEmpty(true)
                    .build();
            ResourceValidator sut = createSut();

            DataValidationException exception = assertThrows(
                    DataValidationException.class,
                    () -> sut.getValidatedExtension(file),
                    "Ожидалось исключение, если файл пустой"
            );

            assertAll(
                    () -> assertEquals(FILE_EMPTY_MESSAGE, exception.getMessage(),
                            "Сообщение должно указывать на пустой файл"),
                    () -> verifyNoInteractionsWithAvatarProperties()
            );
        }

        @Test
        @DisplayName("должен бросать исключение, когда размер файла равен нулю")
        void shouldThrowFileEmptyWhenMultipartFileSizeIsZero() {
            MultipartFile file = multipartFileBuilder()
                    .withEmpty(false)
                    .withSize(0L)
                    .build();
            ResourceValidator sut = createSut();

            DataValidationException exception = assertThrows(
                    DataValidationException.class,
                    () -> sut.getValidatedExtension(file),
                    "Ожидалось исключение при нулевом размере файла"
            );

            assertAll(
                    () -> assertEquals(FILE_EMPTY_MESSAGE, exception.getMessage(),
                            "Сообщение должно указывать, что файл пустой"),
                    () -> verifyNoInteractionsWithAvatarProperties()
            );
        }
    }

    @Nested
    @DisplayName("Проверка имени файла")
    class FilenameValidation {

        @Test
        @DisplayName("должен бросать исключение, когда оригинальное имя отсутствует")
        void shouldThrowMissingFilenameWhenOriginalFilenameIsNull() {
            MultipartFile file = multipartFileBuilder()
                    .withOriginalFilename(null)
                    .build();
            ResourceValidator sut = createSut();

            DataValidationException exception = assertThrows(
                    DataValidationException.class,
                    () -> sut.getValidatedExtension(file),
                    "Ожидалось исключение при отсутствии имени файла"
            );

            assertAll(
                    () -> assertEquals(FILENAME_MISSING_MESSAGE, exception.getMessage(),
                            "Сообщение должно указывать, что имя файла не задано"),
                    () -> verifyNoInteractionsWithAvatarProperties()
            );
        }

        @Test
        @DisplayName("должен бросать исключение, когда невозможно определить расширение")
        void shouldThrowExtensionUndeterminedWhenOriginalFilenameHasNoDot() {
            MultipartFile file = multipartFileBuilder()
                    .withOriginalFilename("avatar")
                    .withContentType(AvatarProperties.MIME_TYPE_JPEG)
                    .build();
            mockAllowedMimeTypes(DEFAULT_ALLOWED_MIME_TYPES);
            ResourceValidator sut = createSut();

            DataValidationException exception = assertThrows(
                    DataValidationException.class,
                    () -> sut.getValidatedExtension(file),
                    "Ожидалось исключение, если расширение невозможно определить"
            );

            assertAll(
                    () -> assertEquals(EXTENSION_UNDETERMINED_MESSAGE, exception.getMessage(),
                            "Сообщение должно указывать на недоступность расширения"),
                    () -> verifyAllowedMimeTypesRequestedOnce()
            );
        }
    }

    @Nested
    @DisplayName("Проверка MIME-типа")
    class MimeTypeValidation {

        @Test
        @DisplayName("должен бросать исключение, когда MIME-тип отсутствует")
        void shouldThrowMimeUndeterminedWhenContentTypeIsBlank() {
            MultipartFile file = multipartFileBuilder()
                    .withContentType("   ")
                    .build();
            ResourceValidator sut = createSut();

            DataValidationException exception = assertThrows(
                    DataValidationException.class,
                    () -> sut.getValidatedExtension(file),
                    "Ожидалось исключение при неопределённом MIME-типе"
            );

            assertAll(
                    () -> assertEquals(MIME_UNDETERMINED_MESSAGE, exception.getMessage(),
                            "Сообщение должно указывать на отсутствие MIME-типa"),
                    () -> verifyNoInteractionsWithAvatarProperties()
            );
        }

        @Test
        @DisplayName("должен бросать исключение, когда MIME-тип оканчивается на слеш")
        void shouldThrowMimeInvalidWhenContentTypeEndsWithSlash() {
            MultipartFile file = multipartFileBuilder()
                    .withContentType("image/")
                    .build();
            ResourceValidator sut = createSut();

            DataValidationException exception = assertThrows(
                    DataValidationException.class,
                    () -> sut.getValidatedExtension(file),
                    "Ожидалось исключение при некорректном MIME-типе"
            );

            assertAll(
                    () -> assertEquals(MIME_INVALID_MESSAGE, exception.getMessage(),
                            "Сообщение должно указывать на некорректный MIME-тип"),
                    () -> verifyNoInteractionsWithAvatarProperties()
            );
        }

        @Test
        @DisplayName("должен бросать исключение, когда MIME-тип не относится к изображениям")
        void shouldThrowMimeExpectedImageWhenContentTypeIsNotImage() {
            MultipartFile file = multipartFileBuilder()
                    .withContentType("application/pdf")
                    .build();
            ResourceValidator sut = createSut();

            DataValidationException exception = assertThrows(
                    DataValidationException.class,
                    () -> sut.getValidatedExtension(file),
                    "Ожидалось исключение при не изображении"
            );

            assertAll(
                    () -> assertEquals(MIME_EXPECTED_IMAGE_MESSAGE, exception.getMessage(),
                            "Сообщение должно указывать, что ожидалось изображение"),
                    () -> verifyNoInteractionsWithAvatarProperties()
            );
        }

        @Test
        @DisplayName("должен бросать исключение, когда MIME-тип отсутствует в списке разрешённых")
        void shouldThrowMimeNotAllowedWhenContentTypeIsMissingInAllowList() {
            MultipartFile file = multipartFileBuilder()
                    .withOriginalFilename("photo.tiff")
                    .withContentType("image/tiff")
                    .build();
            mockAllowedMimeTypes(DEFAULT_ALLOWED_MIME_TYPES);
            ResourceValidator sut = createSut();

            DataValidationException exception = assertThrows(
                    DataValidationException.class,
                    () -> sut.getValidatedExtension(file),
                    "Ожидалось исключение при недопустимом MIME-типе"
            );

            assertAll(
                    () -> assertEquals(MIME_NOT_ALLOWED_MESSAGE, exception.getMessage(),
                            "Сообщение должно указывать на недопустимый MIME-тип"),
                    () -> verifyAllowedMimeTypesRequestedOnce()
            );
        }
    }

    @Nested
    @DisplayName("Проверка расширения")
    class ExtensionValidation {

        @Test
        @DisplayName("должен бросать исключение, когда расширение не поддерживается")
        void shouldThrowUnsupportedExtensionWhenExtensionIsNotSupported() {
            MultipartFile file = multipartFileBuilder()
                    .withOriginalFilename("avatar.gif")
                    .withContentType(AvatarProperties.MIME_TYPE_PNG)
                    .build();
            mockAllowedMimeTypes(DEFAULT_ALLOWED_MIME_TYPES);
            ResourceValidator sut = createSut();

            DataValidationException exception = assertThrows(
                    DataValidationException.class,
                    () -> sut.getValidatedExtension(file),
                    "Ожидалось исключение для неподдерживаемого расширения"
            );

            assertAll(
                    () -> assertEquals(EXTENSION_UNSUPPORTED_MESSAGE, exception.getMessage(),
                            "Сообщение должно указывать на неподдерживаемое расширение"),
                    () -> verifyAllowedMimeTypesRequestedOnce()
            );
        }

        @Test
        @DisplayName("должен бросать исключение, когда расширение не соответствует MIME-типу")
        void shouldThrowExtensionMimeMismatchWhenExtensionDoesNotMatchMime() {
            MultipartFile file = multipartFileBuilder()
                    .withOriginalFilename("avatar.jpg")
                    .withContentType(AvatarProperties.MIME_TYPE_PNG)
                    .build();
            mockAllowedMimeTypes(DEFAULT_ALLOWED_MIME_TYPES);
            ResourceValidator sut = createSut();

            DataValidationException exception = assertThrows(
                    DataValidationException.class,
                    () -> sut.getValidatedExtension(file),
                    "Ожидалось исключение при несоответствии расширения MIME"
            );

            assertAll(
                    () -> assertEquals(EXTENSION_MIME_MISMATCH_MESSAGE, exception.getMessage(),
                            "Сообщение должно указывать на конфликт MIME и расширения"),
                    () -> verifyAllowedMimeTypesRequestedOnce()
            );
        }
    }

    @Nested
    @DisplayName("Успешная валидация")
    class SuccessfulValidation {

        @Test
        @DisplayName("должен возвращать 'jpg' для JPEG-файла с верхним регистром")
        void shouldReturnJpgWhenJpegFileHasUpperCaseExtension() {
            MultipartFile file = multipartFileBuilder()
                    .withOriginalFilename("A.JPEG")
                    .withContentType(AvatarProperties.MIME_TYPE_JPEG)
                    .build();
            mockAllowedMimeTypes(DEFAULT_ALLOWED_MIME_TYPES);
            ResourceValidator sut = createSut();

            String extension = sut.getValidatedExtension(file);

            assertAll(
                    () -> assertEquals("jpg", extension,
                            "Расширение должно нормализоваться к 'jpg' для JPEG-файла"),
                    () -> verifyAllowedMimeTypesRequestedOnce()
            );
        }

        @Test
        @DisplayName("должен возвращать 'jpg' при fallback по MIME 'image/jpg'")
        void shouldReturnJpgWhenFallbackMimeTypeProvided() {
            MultipartFile file = multipartFileBuilder()
                    .withOriginalFilename("a.jpeg")
                    .withContentType("image/jpg")
                    .build();
            mockAllowedMimeTypes(List.of("image/jpg"));
            ResourceValidator sut = createSut();

            String extension = sut.getValidatedExtension(file);

            assertAll(
                    () -> assertEquals("jpg", extension,
                            "Расширение должно быть 'jpg' при допустимом MIME 'image/jpg'"),
                    () -> verifyAllowedMimeTypesRequestedOnce()
            );
        }
    }

    private void mockAllowedMimeTypes(List<String> allowedMimeTypes) {
        when(avatarProperties.allowedMimeTypes()).thenReturn(allowedMimeTypes);
    }

    private void verifyAllowedMimeTypesRequestedOnce() {
        verify(avatarProperties).allowedMimeTypes();
        verifyNoMoreInteractions(avatarProperties);
    }

    private void verifyNoInteractionsWithAvatarProperties() {
        verifyNoInteractions(avatarProperties);
    }

    private static MultipartFileBuilder multipartFileBuilder() {
        return new MultipartFileBuilder();
    }

    private static final class MultipartFileBuilder {

        private String name = DEFAULT_FILE_PARAMETER;
        private String originalFilename = DEFAULT_ORIGINAL_FILENAME;
        private String contentType = DEFAULT_CONTENT_TYPE;
        private byte[] content = DEFAULT_FILE_CONTENT;
        private boolean empty = false;
        private long size = DEFAULT_FILE_SIZE;

        private MultipartFileBuilder withOriginalFilename(String value) {
            this.originalFilename = value;
            return this;
        }

        private MultipartFileBuilder withContentType(String value) {
            this.contentType = value;
            return this;
        }

        private MultipartFileBuilder withEmpty(boolean value) {
            this.empty = value;
            return this;
        }

        private MultipartFileBuilder withSize(long value) {
            this.size = value;
            return this;
        }

        private MultipartFile build() {
            return new TestMultipartFile(
                    name,
                    originalFilename,
                    contentType,
                    content,
                    empty,
                    size
            );
        }
    }

    private record TestMultipartFile(
            String name,
            String originalFilename,
            String contentType,
            byte[] content,
            boolean empty,
            long size
    ) implements MultipartFile {

        @Override
        @NonNull
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return empty;
        }

        @Override
        public long getSize() {
            return size;
        }

        @Override
        @NonNull
        public byte[] getBytes() {
            return content.clone();
        }

        @Override
        @NonNull
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(@NonNull File dest) throws IOException {
            throw new IOException("transferTo не поддерживается в тестовой реализации");
        }
    }
}
