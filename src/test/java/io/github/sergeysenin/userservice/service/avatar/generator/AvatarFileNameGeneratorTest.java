package io.github.sergeysenin.userservice.service.avatar.generator;

import io.github.sergeysenin.userservice.config.avatar.AvatarProperties;
import io.github.sergeysenin.userservice.dto.avatar.AvatarObjectPathsDto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DisplayName("AvatarFileNameGenerator")
class AvatarFileNameGeneratorTest {

    private static final Long USER_ID = 42L;
    private static final String FILE_EXTENSION = "png";
    private static final String STORAGE_PATH = "avatars";
    private static final String FIRST_GENERATED_UUID_VALUE = "c2e4c260-42f6-42ec-9ca6-2a3f62b1bb5f";
    private static final UUID FIRST_GENERATED_UUID = UUID.fromString(FIRST_GENERATED_UUID_VALUE);
    private static final String SECOND_GENERATED_UUID_VALUE = "21962f6f-d0ad-4177-9b9c-2068f4fa0a06";
    private static final UUID SECOND_GENERATED_UUID = UUID.fromString(SECOND_GENERATED_UUID_VALUE);
    private static final String EXPECTED_ORIGINAL_VERSION = "original";
    private static final String EXPECTED_THUMBNAIL_VERSION = "thumbnail";
    private static final String EXPECTED_PROFILE_VERSION = "profile";

    @Mock
    private AvatarProperties avatarProperties;

    @Nested
    @DisplayName("Метод generateFilePaths")
    class GenerateFilePathsTests {

        @Test
        @DisplayName("должен формировать корректные пути ко всем версиям аватара при валидных данных")
        void shouldGenerateExpectedPathsWhenInputIsValid() {
            when(avatarProperties.storagePath()).thenReturn(STORAGE_PATH);
            AvatarFileNameGenerator sut = createSut();

            AvatarObjectPathsDto result;
            try (MockedStatic<UUID> mockedUuid = Mockito.mockStatic(UUID.class)) {
                mockedUuid.when(UUID::randomUUID).thenReturn(FIRST_GENERATED_UUID);

                result = sut.generateFilePaths(USER_ID, FILE_EXTENSION);

                mockedUuid.verify(UUID::randomUUID, only());
            }

            assertAll("Все версии аватара должны иметь корректные пути",
                    () -> assertEquals(
                            buildExpectedPath(FIRST_GENERATED_UUID_VALUE, EXPECTED_ORIGINAL_VERSION, FILE_EXTENSION),
                            result.originalPath(),
                            "Путь к оригинальной версии должен формироваться корректно"
                    ),
                    () -> assertEquals(
                            buildExpectedPath(FIRST_GENERATED_UUID_VALUE, EXPECTED_THUMBNAIL_VERSION, FILE_EXTENSION),
                            result.thumbnailPath(),
                            "Путь к миниатюре должен формироваться корректно"
                    ),
                    () -> assertEquals(
                            buildExpectedPath(FIRST_GENERATED_UUID_VALUE, EXPECTED_PROFILE_VERSION, FILE_EXTENSION),
                            result.profilePath(),
                            "Путь к профайл-версии должен формироваться корректно"
                    ));

            verify(avatarProperties, only()).storagePath();
        }
    }

    @Nested
    @DisplayName("Метод generateFilePaths — множественные вызовы")
    class GenerateFilePathsMultipleInvocationsTests {

        @Test
        @DisplayName("должен генерировать различные UUID при последовательных вызовах")
        void shouldGenerateDistinctUuidWhenInvokedSequentially() {
            when(avatarProperties.storagePath()).thenReturn(STORAGE_PATH);
            AvatarFileNameGenerator sut = createSut();

            AvatarObjectPathsDto firstResult;
            AvatarObjectPathsDto secondResult;

            try (MockedStatic<UUID> mockedUuid = Mockito.mockStatic(UUID.class)) {
                mockedUuid
                        .when(UUID::randomUUID)
                        .thenReturn(FIRST_GENERATED_UUID, SECOND_GENERATED_UUID);

                firstResult = sut.generateFilePaths(USER_ID, FILE_EXTENSION);
                secondResult = sut.generateFilePaths(USER_ID, FILE_EXTENSION);

                mockedUuid.verify(UUID::randomUUID, times(2));
                mockedUuid.verifyNoMoreInteractions();
            }

            assertAll("Каждый вызов должен использовать собственный UUID",
                    () -> assertEquals(
                            buildExpectedPath(FIRST_GENERATED_UUID_VALUE, EXPECTED_ORIGINAL_VERSION, FILE_EXTENSION),
                            firstResult.originalPath(),
                            "Первый вызов должен использовать первый сгенерированный UUID"
                    ),
                    () -> assertEquals(
                            buildExpectedPath(SECOND_GENERATED_UUID_VALUE, EXPECTED_ORIGINAL_VERSION, FILE_EXTENSION),
                            secondResult.originalPath(),
                            "Второй вызов должен использовать второй сгенерированный UUID"
                    ),
                    () -> assertNotEquals(firstResult.originalPath(), secondResult.originalPath(),
                            "Пути оригинальной версии должны отличаться при разных UUID"
                    ),
                    () -> assertNotEquals(firstResult.thumbnailPath(), secondResult.thumbnailPath(),
                            "Пути миниатюр должны отличаться при разных UUID"
                    ),
                    () -> assertNotEquals(firstResult.profilePath(), secondResult.profilePath(),
                            "Пути профайл-версии должны отличаться при разных UUID"
                    ));

            verify(avatarProperties, times(2)).storagePath();
            verifyNoMoreInteractions(avatarProperties);
        }
    }

    private AvatarFileNameGenerator createSut() {
        return new AvatarFileNameGenerator(avatarProperties);
    }

    private String buildExpectedPath(String uuidValue, String version, String fileExtension) {
        return String.join(
                "/",
                STORAGE_PATH,
                String.valueOf(USER_ID),
                uuidValue,
                version + "." + fileExtension
        );
    }
}
