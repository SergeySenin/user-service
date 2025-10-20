package io.github.sergeysenin.userservice.service.avatar;

import io.github.sergeysenin.userservice.config.avatar.AvatarProperties;
import io.github.sergeysenin.userservice.dto.avatar.AvatarObjectPathsDto;
import io.github.sergeysenin.userservice.exception.type.AvatarNotFoundException;
import io.github.sergeysenin.userservice.exception.type.AvatarUploadException;
import io.github.sergeysenin.userservice.mapper.avatar.AvatarMapper;
import io.github.sergeysenin.userservice.service.avatar.generator.AvatarFileNameGenerator;
import io.github.sergeysenin.userservice.service.resource.ResourceService;
import io.github.sergeysenin.userservice.service.s3.S3Service;
import io.github.sergeysenin.userservice.service.user.UserService;
import io.github.sergeysenin.userservice.validator.resource.ResourceValidator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;

import static io.github.sergeysenin.userservice.testutil.avatar.AvatarTestFactory.createAvatarEntity;
import static io.github.sergeysenin.userservice.testutil.multipart.MultipartFileTestUtils.multipartFile;
import static io.github.sergeysenin.userservice.testutil.user.UserTestFactory.createDefaultUser;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DisplayName("AvatarService")
class AvatarServiceTest {

    private static final long USER_ID = 42L;
    private static final String EXTENSION_PNG = "png";
    private static final String ORIGINAL_FILE_NAME = "avatar.png";
    private static final byte[] ORIGINAL_BYTES = {1, 2, 3};
    private static final byte[] THUMBNAIL_BYTES = {11, 12};
    private static final byte[] PROFILE_BYTES = {21, 22};
    private static final String NEW_ORIGINAL_PATH = "avatars/42/new/original.png";
    private static final String NEW_THUMBNAIL_PATH = "avatars/42/new/thumbnail.png";
    private static final String NEW_PROFILE_PATH = "avatars/42/new/profile.png";
    private static final AvatarObjectPathsDto NEW_AVATAR_PATHS = new AvatarObjectPathsDto(
            NEW_ORIGINAL_PATH,
            NEW_THUMBNAIL_PATH,
            NEW_PROFILE_PATH
    );
    private static final AvatarObjectPathsDto OLD_AVATAR_PATHS = new AvatarObjectPathsDto(
            "avatars/42/old/original.png",
            "avatars/42/old/thumbnail.png",
            ""
    );
    private static final AvatarProperties AVATAR_PROPERTIES = new AvatarProperties(
            "avatars",
            new AvatarProperties.AvatarSizesProperties(
                    new AvatarProperties.AvatarSizeProperties(170),
                    new AvatarProperties.AvatarSizeProperties(1080)
            ),
            AvatarProperties.DEFAULT_ALLOWED_MIME_TYPES
    );

    @Mock
    private UserService userService;

    @Mock
    private ResourceService resourceService;

    @Mock
    private S3Service s3Service;

    @Mock
    private ResourceValidator resourceValidator;

    @Mock
    private AvatarMapper avatarMapper;

    @Mock
    private AvatarFileNameGenerator avatarFileNameGenerator;

    private AvatarService createSut() {
        return new AvatarService(
                userService,
                resourceService,
                s3Service,
                resourceValidator,
                avatarMapper,
                AVATAR_PROPERTIES,
                avatarFileNameGenerator
        );
    }

    @Nested
    @DisplayName("uploadAvatar")
    class UploadAvatar {

        @Test
        @DisplayName("должен сохранять новый аватар, когда предыдущий отсутствует")
        void shouldUploadAvatarWhenUserDoesNotHavePreviousAvatar() {
            final var sut = createSut();
            final var file = multipartFile()
                    .withName("avatar")
                    .withOriginalFilename(ORIGINAL_FILE_NAME)
                    .withContentType("image/png")
                    .withContent(ORIGINAL_BYTES)
                    .build();
            final var user = createDefaultUser(null);
            final var mappedAvatar = createAvatarEntity(NEW_AVATAR_PATHS);

            when(userService.getUserByIdOrThrow(USER_ID)).thenReturn(user);
            when(resourceValidator.getValidatedExtension(file)).thenReturn(EXTENSION_PNG);
            when(resourceService.resize(ORIGINAL_BYTES, 170, EXTENSION_PNG)).thenReturn(THUMBNAIL_BYTES);
            when(resourceService.resize(ORIGINAL_BYTES, 1080, EXTENSION_PNG)).thenReturn(PROFILE_BYTES);
            when(avatarFileNameGenerator.generateFilePaths(USER_ID, EXTENSION_PNG)).thenReturn(NEW_AVATAR_PATHS);
            when(avatarMapper.toDto(isNull())).thenReturn(null);
            when(avatarMapper.toEntity(NEW_AVATAR_PATHS)).thenReturn(mappedAvatar);
            when(userService.save(user)).thenReturn(user);

            final var response = sut.uploadAvatar(USER_ID, file);

            assertAll("Проверка ответа при загрузке нового аватара без предыдущего",
                    () -> assertEquals(USER_ID, response.userId(),
                            "В ответе должен содержаться идентификатор пользователя"),
                    () -> assertEquals(NEW_AVATAR_PATHS, response.fileIds(),
                            "Должны возвращаться пути загруженных файлов"),
                    () -> assertNull(response.updatedAt(),
                            "Дата обновления должна совпадать с сохранённым пользователем"),
                    () -> assertEquals(mappedAvatar, user.getUserProfileAvatar(),
                            "У пользователя должен быть установлен новый аватар")
            );

            verify(userService).getUserByIdOrThrow(USER_ID);
            verify(resourceValidator).getValidatedExtension(file);
            verify(resourceService).resize(ORIGINAL_BYTES, 170, EXTENSION_PNG);
            verify(resourceService).resize(ORIGINAL_BYTES, 1080, EXTENSION_PNG);
            verify(avatarFileNameGenerator).generateFilePaths(USER_ID, EXTENSION_PNG);
            verify(avatarMapper).toDto(null);
            verify(avatarMapper).toEntity(NEW_AVATAR_PATHS);
            verify(s3Service).storeObject(NEW_THUMBNAIL_PATH, THUMBNAIL_BYTES);
            verify(s3Service).storeObject(NEW_PROFILE_PATH, PROFILE_BYTES);
            verify(s3Service).storeObject(NEW_ORIGINAL_PATH, ORIGINAL_BYTES);
            verify(userService).save(user);
            verifyNoMoreInteractions(
                    userService,
                    s3Service,
                    resourceService,
                    resourceValidator,
                    avatarFileNameGenerator,
                    avatarMapper
            );
        }

        @Test
        @DisplayName("должен обновлять аватар и удалять старые файлы")
        void shouldReplaceAvatarAndDeletePreviousFilesWhenUserHadAvatar() {
            final var sut = createSut();
            final var file = multipartFile()
                    .withName("avatar")
                    .withOriginalFilename(ORIGINAL_FILE_NAME)
                    .withContentType("image/png")
                    .withContent(ORIGINAL_BYTES)
                    .build();
            final var oldAvatar = createAvatarEntity(OLD_AVATAR_PATHS);
            final var user = createDefaultUser(oldAvatar);
            final var mappedAvatar = createAvatarEntity(NEW_AVATAR_PATHS);

            when(userService.getUserByIdOrThrow(USER_ID)).thenReturn(user);
            when(resourceValidator.getValidatedExtension(file)).thenReturn(EXTENSION_PNG);
            when(resourceService.resize(ORIGINAL_BYTES, 170, EXTENSION_PNG)).thenReturn(THUMBNAIL_BYTES);
            when(resourceService.resize(ORIGINAL_BYTES, 1080, EXTENSION_PNG)).thenReturn(PROFILE_BYTES);
            when(avatarFileNameGenerator.generateFilePaths(USER_ID, EXTENSION_PNG)).thenReturn(NEW_AVATAR_PATHS);
            when(avatarMapper.toDto(oldAvatar)).thenReturn(OLD_AVATAR_PATHS);
            when(avatarMapper.toEntity(NEW_AVATAR_PATHS)).thenReturn(mappedAvatar);
            when(userService.save(user)).thenReturn(user);

            final var response = sut.uploadAvatar(USER_ID, file);

            assertAll("Проверка обновления существующего аватара",
                    () -> assertEquals(NEW_AVATAR_PATHS, response.fileIds(),
                            "Ответ должен содержать пути новых файлов"),
                    () -> assertEquals(mappedAvatar, user.getUserProfileAvatar(),
                            "У пользователя должен быть установлен новый аватар")
            );

            verify(userService).getUserByIdOrThrow(USER_ID);
            verify(resourceValidator).getValidatedExtension(file);
            verify(resourceService).resize(ORIGINAL_BYTES, 170, EXTENSION_PNG);
            verify(resourceService).resize(ORIGINAL_BYTES, 1080, EXTENSION_PNG);
            verify(avatarFileNameGenerator).generateFilePaths(USER_ID, EXTENSION_PNG);
            verify(avatarMapper).toDto(oldAvatar);
            verify(avatarMapper).toEntity(NEW_AVATAR_PATHS);
            verify(s3Service).storeObject(NEW_THUMBNAIL_PATH, THUMBNAIL_BYTES);
            verify(s3Service).storeObject(NEW_PROFILE_PATH, PROFILE_BYTES);
            verify(s3Service).storeObject(NEW_ORIGINAL_PATH, ORIGINAL_BYTES);
            verify(s3Service).removeObject(OLD_AVATAR_PATHS.originalPath());
            verify(s3Service).removeObject(OLD_AVATAR_PATHS.thumbnailPath());
            verify(s3Service, never()).removeObject(OLD_AVATAR_PATHS.profilePath());
            verify(userService).save(user);
            verifyNoMoreInteractions(
                    userService,
                    s3Service,
                    resourceService,
                    resourceValidator,
                    avatarFileNameGenerator,
                    avatarMapper
            );
        }

        @Test
        @DisplayName("должен бросать AvatarUploadException, когда чтение файла завершилось с ошибкой")
        void shouldThrowAvatarUploadExceptionWhenFileCannotBeRead() {
            final var sut = createSut();
            final var ioException = new IOException("read failed");
            final var file = multipartFile()
                    .withName("avatar")
                    .withOriginalFilename(ORIGINAL_FILE_NAME)
                    .withContentType("image/png")
                    .withContent(ORIGINAL_BYTES)
                    .withBytesException(ioException)
                    .build();
            final var user = createDefaultUser(null);

            when(userService.getUserByIdOrThrow(USER_ID)).thenReturn(user);
            when(resourceValidator.getValidatedExtension(file)).thenReturn(EXTENSION_PNG);

            final var exception = assertThrows(
                    AvatarUploadException.class,
                    () -> sut.uploadAvatar(USER_ID, file),
                    "Ожидалось исключение при ошибке чтения файла"
            );

            assertAll("Проверка исключения при ошибке чтения файла",
                    () -> assertEquals("Ошибка чтения файла аватара", exception.getMessage(),
                            "Сообщение должно указывать на ошибку чтения"),
                    () -> assertEquals(ioException, exception.getCause(),
                            "Причина должна содержать исходное исключение")
            );

            verify(userService).getUserByIdOrThrow(USER_ID);
            verify(resourceValidator).getValidatedExtension(file);
            verifyNoMoreInteractions(userService, resourceValidator);
            verifyNoInteractions(s3Service, resourceService, avatarFileNameGenerator, avatarMapper);
        }
    }

    @Nested
    @DisplayName("getAvatar")
    class GetAvatar {

        @Test
        @DisplayName("должен возвращать ссылки на аватар, когда он существует")
        void shouldReturnAvatarUrlsWhenAvatarExists() {
            final var sut = createSut();
            final var avatar = createAvatarEntity(NEW_AVATAR_PATHS);
            final var user = createDefaultUser(avatar);

            when(userService.getUserByIdOrThrow(USER_ID)).thenReturn(user);
            when(s3Service.generatePresignedUrl(NEW_ORIGINAL_PATH)).thenReturn("original-url");
            when(s3Service.generatePresignedUrl(NEW_THUMBNAIL_PATH)).thenReturn("thumbnail-url");
            when(s3Service.generatePresignedUrl(NEW_PROFILE_PATH)).thenReturn("profile-url");

            final var response = sut.getAvatar(USER_ID);

            assertAll("Проверка ответа с доступным аватаром",
                    () -> assertEquals(USER_ID, response.userId(),
                            "В ответе должен быть идентификатор пользователя"),
                    () -> assertTrue(response.hasAvatar(),
                            "Флаг наличия аватара должен быть установлен"),
                    () -> {
                        final var paths = response.fileIds();
                        assertNotNull(paths, "Пути не должны быть null");
                        assertAll("Проверка подписей для всех ссылок",
                                () -> assertEquals("original-url", paths.originalPath(),
                                        "Должна возвращаться ссылка на оригинал"),
                                () -> assertEquals("thumbnail-url", paths.thumbnailPath(),
                                        "Должна возвращаться ссылка на миниатюру"),
                                () -> assertEquals("profile-url", paths.profilePath(),
                                        "Должна возвращаться ссылка на профиль"));
                    }
            );

            verify(userService).getUserByIdOrThrow(USER_ID);
            verify(s3Service).generatePresignedUrl(NEW_ORIGINAL_PATH);
            verify(s3Service).generatePresignedUrl(NEW_THUMBNAIL_PATH);
            verify(s3Service).generatePresignedUrl(NEW_PROFILE_PATH);
            verifyNoMoreInteractions(userService, s3Service);
            verifyNoInteractions(resourceService, resourceValidator, avatarFileNameGenerator, avatarMapper);
        }

        @Test
        @DisplayName("должен обнулять ссылку, если путь пустой")
        void shouldSkipPresignedUrlGenerationWhenPathBlank() {
            final var sut = createSut();
            final var avatar = createAvatarEntity(new AvatarObjectPathsDto(
                    NEW_ORIGINAL_PATH,
                    " ",
                    null
            ));
            final var user = createDefaultUser(avatar);

            when(userService.getUserByIdOrThrow(USER_ID)).thenReturn(user);
            when(s3Service.generatePresignedUrl(NEW_ORIGINAL_PATH)).thenReturn("original-url");

            final var response = sut.getAvatar(USER_ID);

            assertAll("Проверка поведения при пустых путях аватара",
                    () -> assertEquals("original-url", response.fileIds().originalPath(),
                            "Должна возвращаться ссылка только для непустого пути"),
                    () -> assertNull(response.fileIds().thumbnailPath(),
                            "Пустой путь должен приводить к null ссылке"),
                    () -> assertNull(response.fileIds().profilePath(),
                            "Отсутствующий путь должен приводить к null ссылке")
            );

            verify(userService).getUserByIdOrThrow(USER_ID);
            verify(s3Service).generatePresignedUrl(NEW_ORIGINAL_PATH);
            verifyNoMoreInteractions(userService, s3Service);
            verifyNoInteractions(resourceService, resourceValidator, avatarFileNameGenerator, avatarMapper);
        }

        @Test
        @DisplayName("должен бросать AvatarNotFoundException, когда аватар отсутствует")
        void shouldThrowAvatarNotFoundExceptionWhenAvatarMissing() {
            final var sut = createSut();
            final var user = createDefaultUser(null);

            when(userService.getUserByIdOrThrow(USER_ID)).thenReturn(user);

            final var exception = assertThrows(
                    AvatarNotFoundException.class,
                    () -> sut.getAvatar(USER_ID),
                    "Ожидалось исключение при отсутствии аватара"
            );

            assertEquals("Аватар пользователя не найден: id=" + USER_ID, exception.getMessage(),
                    "Сообщение должно содержать идентификатор пользователя");

            verify(userService).getUserByIdOrThrow(USER_ID);
            verifyNoMoreInteractions(userService);
            verifyNoInteractions(s3Service, resourceService, resourceValidator, avatarFileNameGenerator, avatarMapper);
        }
    }

    @Nested
    @DisplayName("deleteAvatar")
    class DeleteAvatar {

        @Test
        @DisplayName("должен удалять аватар и возвращать подтверждение")
        void shouldDeleteAvatarAndReturnConfirmation() {
            final var sut = createSut();
            final var avatar = createAvatarEntity(NEW_AVATAR_PATHS);
            final var user = createDefaultUser(avatar);

            when(userService.getUserByIdOrThrow(USER_ID)).thenReturn(user);
            when(avatarMapper.toDto(avatar)).thenReturn(NEW_AVATAR_PATHS);

            final var response = sut.deleteAvatar(USER_ID);

            assertAll("Проверка ответа после удаления аватара",
                    () -> assertEquals(USER_ID, response.userId(),
                            "В ответе должен быть идентификатор пользователя"),
                    () -> assertTrue(response.removed(),
                            "Флаг удаления должен быть установлен"),
                    () -> assertEquals(NEW_AVATAR_PATHS, response.removedFiles(),
                            "Ответ должен содержать пути удалённых файлов"),
                    () -> assertNull(user.getUserProfileAvatar(),
                            "У пользователя должен быть очищен аватар")
            );

            verify(userService).getUserByIdOrThrow(USER_ID);
            verify(avatarMapper).toDto(avatar);
            verify(s3Service).removeObject(NEW_ORIGINAL_PATH);
            verify(s3Service).removeObject(NEW_THUMBNAIL_PATH);
            verify(s3Service).removeObject(NEW_PROFILE_PATH);
            verify(userService).save(user);
            verifyNoMoreInteractions(userService, s3Service, avatarMapper);
            verifyNoInteractions(resourceService, resourceValidator, avatarFileNameGenerator);
        }

        @Test
        @DisplayName("должен бросать AvatarNotFoundException, когда нечего удалять")
        void shouldThrowAvatarNotFoundExceptionWhenDeletingMissingAvatar() {
            final var sut = createSut();
            final var user = createDefaultUser(null);

            when(userService.getUserByIdOrThrow(USER_ID)).thenReturn(user);

            final var exception = assertThrows(
                    AvatarNotFoundException.class,
                    () -> sut.deleteAvatar(USER_ID),
                    "Ожидалось исключение при попытке удалить отсутствующий аватар"
            );

            assertEquals("Аватар пользователя не найден: id=" + USER_ID, exception.getMessage(),
                    "Сообщение должно содержать идентификатор пользователя");

            verify(userService).getUserByIdOrThrow(USER_ID);
            verifyNoMoreInteractions(userService);
            verifyNoInteractions(s3Service, resourceService, resourceValidator, avatarFileNameGenerator, avatarMapper);
        }
    }
}
