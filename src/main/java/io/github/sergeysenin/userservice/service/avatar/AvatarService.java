package io.github.sergeysenin.userservice.service.avatar;

import io.github.sergeysenin.userservice.config.avatar.AvatarProperties;
import io.github.sergeysenin.userservice.dto.avatar.GetAvatarResponse;
import io.github.sergeysenin.userservice.dto.avatar.UploadAvatarResponse;
import io.github.sergeysenin.userservice.dto.avatar.DeleteAvatarResponse;
import io.github.sergeysenin.userservice.dto.avatar.AvatarObjectPathsDto;
import io.github.sergeysenin.userservice.entity.user.User;
import io.github.sergeysenin.userservice.entity.user.UserProfileAvatar;
import io.github.sergeysenin.userservice.exception.type.AvatarNotFoundException;
import io.github.sergeysenin.userservice.exception.type.AvatarUploadException;
import io.github.sergeysenin.userservice.mapper.avatar.AvatarMapper;
import io.github.sergeysenin.userservice.service.avatar.generator.AvatarFileNameGenerator;
import io.github.sergeysenin.userservice.service.resource.ResourceService;
import io.github.sergeysenin.userservice.service.s3.S3Service;
import io.github.sergeysenin.userservice.service.user.UserService;
import io.github.sergeysenin.userservice.validator.resource.ResourceValidationResult;
import io.github.sergeysenin.userservice.validator.resource.ResourceValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvatarService {

    private final UserService userService;
    private final ResourceService resourceService;
    private final S3Service s3Service;
    private final ResourceValidator resourceValidator;
    private final AvatarMapper avatarMapper;
    private final AvatarProperties avatarProperties;
    private final AvatarFileNameGenerator avatarFileNameGenerator;

    @Transactional
    public UploadAvatarResponse uploadAvatar(Long userId, MultipartFile file) {
        log.debug("Начало загрузки аватара пользователя: userId={}", userId);

        var user = userService.getUserByIdOrThrow(userId);

        var resourceMetadata = resourceValidator.validateResource(file);
        byte[] originalBytes = readFileBytes(file, userId);

        var oldAvatarPaths = avatarMapper.toDto(user.getUserProfileAvatar());
        var newAvatarPaths = uploadResizedVersions(userId, originalBytes, resourceMetadata);

        applyAvatarToUser(user, newAvatarPaths);

        var savedUser = userService.save(user);

        if (oldAvatarPaths != null) {
            try {
                deleteAvatarObjects(oldAvatarPaths);
            } catch (Exception exception) {
                log.warn("Не удалось удалить старые файлы аватара пользователя: userId={}", userId, exception);
            }
        }

        log.info("Аватар пользователя успешно загружен: userId={}, files={}", userId, newAvatarPaths);

        return new UploadAvatarResponse(userId, newAvatarPaths, savedUser.getUpdatedAt());
    }

    @Transactional(readOnly = true)
    public GetAvatarResponse getAvatar(Long userId) {
        log.debug("Начало получения аватара пользователя: userId={}", userId);

        var user = userService.getUserByIdOrThrow(userId);
        var avatar = ensureAvatarExists(
                user.getUserProfileAvatar(),
                userId,
                "Аватар для пользователя не найден: userId={}"
        );

        var response = buildGetAvatarResponse(userId, avatar);

        log.info("Аватар пользователя успешно получен: userId={}, hasAvatar={}", userId, true);

        return response;
    }

    @Transactional
    public DeleteAvatarResponse deleteAvatar(Long userId) {
        log.debug("Начало удаления аватара пользователя: userId={}", userId);

        var user = userService.getUserByIdOrThrow(userId);
        var avatar = ensureAvatarExists(
                user.getUserProfileAvatar(),
                userId,
                "Попытка удалить отсутствующий аватар пользователя: userId={}"
        );

        var removedPaths = avatarMapper.toDto(avatar);

        deleteAvatarObjects(removedPaths);

        user.updateAvatar(null);
        userService.save(user);

        log.info("Аватар пользователя успешно удален: userId={}", userId);

        return new DeleteAvatarResponse(userId, true, removedPaths);
    }

    private byte[] readFileBytes(MultipartFile file, Long userId) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            log.error("Не удалось прочитать содержимое файла аватара: userId={}, fileName={}",
                    userId, file.getOriginalFilename(), exception);
            throw new AvatarUploadException("Ошибка чтения файла аватара", exception);
        }
    }

    private AvatarObjectPathsDto uploadResizedVersions(
            Long userId,
            byte[] originalBytes,
            ResourceValidationResult resourceMetadata
    ) {
        String extension = resourceMetadata.canonicalExtension();
        String mimeType = resourceMetadata.mimeType();
        var newAvatarPaths = avatarFileNameGenerator.generateFilePaths(userId, extension);

        int thumbnailMaxSide = avatarProperties.sizes().thumbnail().maxSide();
        int profileMaxSide = avatarProperties.sizes().profile().maxSide();

        byte[] thumbnailBytes = resourceService.resize(originalBytes, thumbnailMaxSide, extension);
        byte[] profileBytes = resourceService.resize(originalBytes, profileMaxSide, extension);

        s3Service.storeObject(newAvatarPaths.thumbnailPath(), thumbnailBytes, mimeType);
        s3Service.storeObject(newAvatarPaths.profilePath(), profileBytes, mimeType);
        s3Service.storeObject(newAvatarPaths.originalPath(), originalBytes, mimeType);

        return newAvatarPaths;
    }

    private void applyAvatarToUser(User user, AvatarObjectPathsDto newAvatarPaths) {
        UserProfileAvatar avatar = avatarMapper.toEntity(newAvatarPaths);
        user.updateAvatar(avatar);
    }

    private UserProfileAvatar ensureAvatarExists(UserProfileAvatar avatar, Long userId, String logMessage) {
        if (avatar == null || !StringUtils.hasText(avatar.getOriginalPath())) {
            log.warn(logMessage, userId);
            throw new AvatarNotFoundException("Аватар пользователя не найден: id=" + userId);
        }

        return avatar;
    }

    private GetAvatarResponse buildGetAvatarResponse(Long userId, UserProfileAvatar avatar) {
        String originalUrl = generatePresignedUrl(avatar.getOriginalPath());
        String thumbnailUrl = generatePresignedUrl(avatar.getThumbnailPath());
        String profileUrl = generatePresignedUrl(avatar.getProfilePath());

        var responsePaths = new AvatarObjectPathsDto(originalUrl, thumbnailUrl, profileUrl);

        return new GetAvatarResponse(userId, responsePaths, true);
    }

    private String generatePresignedUrl(String path) {
        return StringUtils.hasText(path) ? s3Service.generatePresignedUrl(path) : null;
    }

    private void deleteAvatarObjects(AvatarObjectPathsDto paths) {
        if (paths == null) {
            return;
        }

        Stream.of(paths.originalPath(), paths.thumbnailPath(), paths.profilePath())
                .filter(StringUtils::hasText)
                .forEach(s3Service::removeObject);
    }
}
