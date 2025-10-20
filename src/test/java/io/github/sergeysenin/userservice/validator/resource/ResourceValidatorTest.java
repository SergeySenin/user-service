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

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static io.github.sergeysenin.userservice.testutil.resource.ResourceValidatorTestData.avatarMultipartFileBuilder;
import static io.github.sergeysenin.userservice.testutil.resource.ResourceValidatorTestData.mockAllowedMimeTypes;
import static io.github.sergeysenin.userservice.testutil.resource.ResourceValidatorTestData.verifyAllowedMimeTypesRequestedOnce;
import static io.github.sergeysenin.userservice.testutil.resource.ResourceValidatorTestData.verifyNoInteractionsWithAvatarProperties;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
            var sut = createSut();

            DataValidationException exception = assertThrows(
                    DataValidationException.class,
                    () -> sut.getValidatedExtension(null),
                    "Ожидалось исключение, если файл отсутствует"
            );

            assertAll("Проверка исключения при отсутствии файла",
                    () -> assertEquals(FILE_NOT_FOUND_MESSAGE, exception.getMessage(),
                            "Сообщение должно указывать, что файл не найден"),
                    () -> verifyNoInteractionsWithAvatarProperties(avatarProperties)
            );
        }

        @Test
        @DisplayName("должен бросать исключение, когда файл не содержит данных")
        void shouldThrowFileEmptyWhenMultipartFileHasNoContent() {
            MultipartFile file = avatarMultipartFileBuilder(
                    DEFAULT_FILE_PARAMETER,
                    DEFAULT_ORIGINAL_FILENAME,
                    DEFAULT_CONTENT_TYPE,
                    DEFAULT_FILE_CONTENT
            )
                    .withSize(0L)
                    .build();
            var sut = createSut();

            DataValidationException exception = assertThrows(
                    DataValidationException.class,
                    () -> sut.getValidatedExtension(file),
                    "Ожидалось исключение при отсутствии данных файла"
            );

            assertAll("Проверка исключения при пустом файле",
                    () -> assertEquals(FILE_EMPTY_MESSAGE, exception.getMessage(),
                            "Сообщение должно указывать на пустой файл"),
                    () -> verifyNoInteractionsWithAvatarProperties(avatarProperties)
            );
        }
    }

    @Nested
    @DisplayName("Проверка имени файла")
    class FilenameValidation {

        @Test
        @DisplayName("должен бросать исключение, когда оригинальное имя отсутствует")
        void shouldThrowMissingFilenameWhenOriginalFilenameIsNull() {
            MultipartFile file = avatarMultipartFileBuilder(
                    DEFAULT_FILE_PARAMETER,
                    DEFAULT_ORIGINAL_FILENAME,
                    DEFAULT_CONTENT_TYPE,
                    DEFAULT_FILE_CONTENT
            )
                    .withOriginalFilename(null)
                    .build();
            var sut = createSut();

            DataValidationException exception = assertThrows(
                    DataValidationException.class,
                    () -> sut.getValidatedExtension(file),
                    "Ожидалось исключение при отсутствии имени файла"
            );

            assertAll("Проверка исключения при отсутствии имени файла",
                    () -> assertEquals(FILENAME_MISSING_MESSAGE, exception.getMessage(),
                            "Сообщение должно указывать, что имя файла не задано"),
                    () -> verifyNoInteractionsWithAvatarProperties(avatarProperties)
            );
        }

        @Test
        @DisplayName("должен бросать исключение, когда невозможно определить расширение")
        void shouldThrowExtensionUndeterminedWhenOriginalFilenameHasNoDot() {
            MultipartFile file = avatarMultipartFileBuilder(
                    DEFAULT_FILE_PARAMETER,
                    DEFAULT_ORIGINAL_FILENAME,
                    DEFAULT_CONTENT_TYPE,
                    DEFAULT_FILE_CONTENT
            )
                    .withOriginalFilename("avatar")
                    .withContentType(AvatarProperties.MIME_TYPE_JPEG)
                    .build();
            mockAllowedMimeTypes(avatarProperties, DEFAULT_ALLOWED_MIME_TYPES);
            var sut = createSut();

            DataValidationException exception = assertThrows(
                    DataValidationException.class,
                    () -> sut.getValidatedExtension(file),
                    "Ожидалось исключение, если расширение невозможно определить"
            );

            assertAll("Проверка исключения при отсутствии расширения",
                    () -> assertEquals(EXTENSION_UNDETERMINED_MESSAGE, exception.getMessage(),
                            "Сообщение должно указывать на недоступность расширения"),
                    () -> verifyAllowedMimeTypesRequestedOnce(avatarProperties)
            );
        }
    }

    @Nested
    @DisplayName("Проверка MIME-типа")
    class MimeTypeValidation {

        @Test
        @DisplayName("должен бросать исключение, когда MIME-тип отсутствует")
        void shouldThrowMimeUndeterminedWhenContentTypeIsBlank() {
            MultipartFile file = avatarMultipartFileBuilder(
                    DEFAULT_FILE_PARAMETER,
                    DEFAULT_ORIGINAL_FILENAME,
                    DEFAULT_CONTENT_TYPE,
                    DEFAULT_FILE_CONTENT
            )
                    .withContentType("   ")
                    .build();
            var sut = createSut();

            DataValidationException exception = assertThrows(
                    DataValidationException.class,
                    () -> sut.getValidatedExtension(file),
                    "Ожидалось исключение при неопределённом MIME-типе"
            );

            assertAll("Проверка исключения при неопределённом MIME",
                    () -> assertEquals(MIME_UNDETERMINED_MESSAGE, exception.getMessage(),
                            "Сообщение должно указывать на отсутствие MIME-типa"),
                    () -> verifyNoInteractionsWithAvatarProperties(avatarProperties)
            );
        }

        @Test
        @DisplayName("должен бросать исключение, когда MIME-тип оканчивается на слеш")
        void shouldThrowMimeInvalidWhenContentTypeEndsWithSlash() {
            MultipartFile file = avatarMultipartFileBuilder(
                    DEFAULT_FILE_PARAMETER,
                    DEFAULT_ORIGINAL_FILENAME,
                    DEFAULT_CONTENT_TYPE,
                    DEFAULT_FILE_CONTENT
            )
                    .withContentType("image/")
                    .build();
            var sut = createSut();

            DataValidationException exception = assertThrows(
                    DataValidationException.class,
                    () -> sut.getValidatedExtension(file),
                    "Ожидалось исключение при некорректном MIME-типе"
            );

            assertAll("Проверка исключения при некорректном MIME",
                    () -> assertEquals(MIME_INVALID_MESSAGE, exception.getMessage(),
                            "Сообщение должно указывать на некорректный MIME-тип"),
                    () -> verifyNoInteractionsWithAvatarProperties(avatarProperties)
            );
        }

        @Test
        @DisplayName("должен бросать исключение, когда MIME-тип не относится к изображениям")
        void shouldThrowMimeExpectedImageWhenContentTypeIsNotImage() {
            MultipartFile file = avatarMultipartFileBuilder(
                    DEFAULT_FILE_PARAMETER,
                    DEFAULT_ORIGINAL_FILENAME,
                    DEFAULT_CONTENT_TYPE,
                    DEFAULT_FILE_CONTENT
            )
                    .withContentType("application/pdf")
                    .build();
            var sut = createSut();

            DataValidationException exception = assertThrows(
                    DataValidationException.class,
                    () -> sut.getValidatedExtension(file),
                    "Ожидалось исключение при не изображении"
            );

            assertAll("Проверка исключения при MIME не изображения",
                    () -> assertEquals(MIME_EXPECTED_IMAGE_MESSAGE, exception.getMessage(),
                            "Сообщение должно указывать, что ожидалось изображение"),
                    () -> verifyNoInteractionsWithAvatarProperties(avatarProperties)
            );
        }

        @Test
        @DisplayName("должен бросать исключение, когда MIME-тип отсутствует в списке разрешённых")
        void shouldThrowMimeNotAllowedWhenContentTypeIsMissingInAllowList() {
            MultipartFile file = avatarMultipartFileBuilder(
                    DEFAULT_FILE_PARAMETER,
                    DEFAULT_ORIGINAL_FILENAME,
                    DEFAULT_CONTENT_TYPE,
                    DEFAULT_FILE_CONTENT
            )
                    .withOriginalFilename("photo.tiff")
                    .withContentType("image/tiff")
                    .build();
            mockAllowedMimeTypes(avatarProperties, DEFAULT_ALLOWED_MIME_TYPES);
            var sut = createSut();

            DataValidationException exception = assertThrows(
                    DataValidationException.class,
                    () -> sut.getValidatedExtension(file),
                    "Ожидалось исключение при недопустимом MIME-типе"
            );

            assertAll("Проверка исключения при недопустимом MIME",
                    () -> assertEquals(MIME_NOT_ALLOWED_MESSAGE, exception.getMessage(),
                            "Сообщение должно указывать на недопустимый MIME-тип"),
                    () -> verifyAllowedMimeTypesRequestedOnce(avatarProperties)
            );
        }
    }

    @Test
    @DisplayName("должен бросать исключение, когда список разрешённых MIME-типов пуст")
    void shouldThrowMimeNotAllowedWhenAllowListIsEmpty() {
        MultipartFile file = avatarMultipartFileBuilder(
                DEFAULT_FILE_PARAMETER,
                DEFAULT_ORIGINAL_FILENAME,
                DEFAULT_CONTENT_TYPE,
                DEFAULT_FILE_CONTENT
        )
                .withOriginalFilename("avatar.png")
                .withContentType(AvatarProperties.MIME_TYPE_PNG)
                .build();
        mockAllowedMimeTypes(avatarProperties, List.of());
        var sut = createSut();

        DataValidationException exception = assertThrows(
                DataValidationException.class,
                () -> sut.getValidatedExtension(file),
                "Ожидалось исключение при пустом списке MIME-типов"
        );

        assertAll("Проверка исключения при пустом списке MIME",
                () -> assertEquals(MIME_NOT_ALLOWED_MESSAGE, exception.getMessage(),
                        "Сообщение должно указывать на недопустимый MIME-тип"),
                () -> verifyAllowedMimeTypesRequestedOnce(avatarProperties)
        );
    }

    @Nested
    @DisplayName("Проверка расширения")
    class ExtensionValidation {

        @Test
        @DisplayName("должен бросать исключение, когда расширение не поддерживается")
        void shouldThrowUnsupportedExtensionWhenExtensionIsNotSupported() {
            MultipartFile file = avatarMultipartFileBuilder(
                    DEFAULT_FILE_PARAMETER,
                    DEFAULT_ORIGINAL_FILENAME,
                    DEFAULT_CONTENT_TYPE,
                    DEFAULT_FILE_CONTENT
            )
                    .withOriginalFilename("avatar.gif")
                    .withContentType(AvatarProperties.MIME_TYPE_PNG)
                    .build();
            mockAllowedMimeTypes(avatarProperties, DEFAULT_ALLOWED_MIME_TYPES);
            var sut = createSut();

            DataValidationException exception = assertThrows(
                    DataValidationException.class,
                    () -> sut.getValidatedExtension(file),
                    "Ожидалось исключение для неподдерживаемого расширения"
            );

            assertAll("Проверка исключения при неподдерживаемом расширении",
                    () -> assertEquals(EXTENSION_UNSUPPORTED_MESSAGE, exception.getMessage(),
                            "Сообщение должно указывать на неподдерживаемое расширение"),
                    () -> verifyAllowedMimeTypesRequestedOnce(avatarProperties)
            );
        }

        @Test
        @DisplayName("должен бросать исключение, когда расширение не соответствует MIME-типу")
        void shouldThrowExtensionMimeMismatchWhenExtensionDoesNotMatchMime() {
            MultipartFile file = avatarMultipartFileBuilder(
                    DEFAULT_FILE_PARAMETER,
                    DEFAULT_ORIGINAL_FILENAME,
                    DEFAULT_CONTENT_TYPE,
                    DEFAULT_FILE_CONTENT
            )
                    .withOriginalFilename("avatar.jpg")
                    .withContentType(AvatarProperties.MIME_TYPE_PNG)
                    .build();
            mockAllowedMimeTypes(avatarProperties, DEFAULT_ALLOWED_MIME_TYPES);
            var sut = createSut();

            DataValidationException exception = assertThrows(
                    DataValidationException.class,
                    () -> sut.getValidatedExtension(file),
                    "Ожидалось исключение при несоответствии расширения MIME"
            );

            assertAll("Проверка исключения при конфликте MIME и расширения",
                    () -> assertEquals(EXTENSION_MIME_MISMATCH_MESSAGE, exception.getMessage(),
                            "Сообщение должно указывать на конфликт MIME и расширения"),
                    () -> verifyAllowedMimeTypesRequestedOnce(avatarProperties)
            );
        }
    }

    @Nested
    @DisplayName("Успешная валидация")
    class SuccessfulValidation {

        @Test
        @DisplayName("должен возвращать 'jpg' для JPEG-файла с верхним регистром")
        void shouldReturnJpgWhenJpegFileHasUpperCaseExtension() {
            MultipartFile file = avatarMultipartFileBuilder(
                    DEFAULT_FILE_PARAMETER,
                    DEFAULT_ORIGINAL_FILENAME,
                    DEFAULT_CONTENT_TYPE,
                    DEFAULT_FILE_CONTENT
            )
                    .withOriginalFilename("A.JPEG")
                    .withContentType(AvatarProperties.MIME_TYPE_JPEG)
                    .build();
            mockAllowedMimeTypes(avatarProperties, DEFAULT_ALLOWED_MIME_TYPES);
            var sut = createSut();

            String extension = sut.getValidatedExtension(file);

            assertAll("Проверка успешной нормализации расширения",
                    () -> assertEquals("jpg", extension,
                            "Расширение должно нормализоваться к 'jpg' для JPEG-файла"),
                    () -> verifyAllowedMimeTypesRequestedOnce(avatarProperties)
            );
        }

        @Test
        @DisplayName("должен возвращать 'jpg' при fallback по MIME 'image/jpg'")
        void shouldReturnJpgWhenFallbackMimeTypeProvided() {
            MultipartFile file = avatarMultipartFileBuilder(
                    DEFAULT_FILE_PARAMETER,
                    DEFAULT_ORIGINAL_FILENAME,
                    DEFAULT_CONTENT_TYPE,
                    DEFAULT_FILE_CONTENT
            )
                    .withOriginalFilename("a.jpeg")
                    .withContentType("image/jpg")
                    .build();
            mockAllowedMimeTypes(avatarProperties, List.of("image/jpg"));
            var sut = createSut();

            String extension = sut.getValidatedExtension(file);

            assertAll("Проверка нормализации расширения при fallback MIME",
                    () -> assertEquals("jpg", extension,
                            "Расширение должно быть 'jpg' при допустимом MIME 'image/jpg'"),
                    () -> verifyAllowedMimeTypesRequestedOnce(avatarProperties)
            );
        }
    }
}
