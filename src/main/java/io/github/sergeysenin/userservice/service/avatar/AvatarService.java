package io.github.sergeysenin.userservice.service.avatar;

import io.github.sergeysenin.userservice.config.avatar.AvatarProperties;
import io.github.sergeysenin.userservice.dto.avatar.UploadAvatarResponse;
import io.github.sergeysenin.userservice.dto.avatar.GetAvatarResponse;
import io.github.sergeysenin.userservice.dto.avatar.DeleteAvatarResponse;
import io.github.sergeysenin.userservice.dto.avatar.AvatarFileIdsDto;
import io.github.sergeysenin.userservice.entity.user.User;
import io.github.sergeysenin.userservice.entity.user.UserProfileAvatar;
import io.github.sergeysenin.userservice.exception.type.AvatarUploadException;
import io.github.sergeysenin.userservice.service.avatar.generator.AvatarFileNameGenerator;
import io.github.sergeysenin.userservice.service.resource.ResourceService;
import io.github.sergeysenin.userservice.service.s3.S3Service;
import io.github.sergeysenin.userservice.service.user.UserService;
import io.github.sergeysenin.userservice.validator.resource.ResourceValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/*
AvatarService — координирует проверку файлов, генерацию имён, ресайз,
работу с хранилищем и обновление профиля пользователя.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AvatarService {

    private final UserService userService;
    private final S3Service s3Service;
    private final ResourceService resourceService;
    private final ResourceValidator resourceValidator;
    private final AvatarFileNameGenerator avatarFileNameGenerator;
    private final AvatarProperties avatarProperties;

    @Transactional
    public UploadAvatarResponse uploadAvatar(Long userId, MultipartFile file) {
        String format = resourceValidator.validateAndExtractExtension(file);
        User user = userService.getExistingUser(userId);

        AvatarFileIdsDto oldAvatarKeys = extractAvatarFileIds(user.getUserProfileAvatar());
        AvatarFileIdsDto newAvatarKeys = avatarFileNameGenerator.generate(userId, format);

        byte[] originalBytes = readFileBytes(file);
        byte[] profileBytes = resourceService.resize(
                originalBytes,
                avatarProperties.sizes().profile().maxSide(),
                format
        );
        byte[] thumbnailBytes = resourceService.resize(
                originalBytes,
                avatarProperties.sizes().thumbnail().maxSide(),
                format
        );

        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }
        uploadFilesWithRollback(newAvatarKeys, originalBytes, profileBytes, thumbnailBytes, contentType);

        try {
            deleteOldFiles(oldAvatarKeys);

            user.updateAvatar(UserProfileAvatar.builder()
                    .originalPath(newAvatarKeys.originalKey())
                    .profilePath(newAvatarKeys.profileKey())
                    .thumbnailPath(newAvatarKeys.thumbnailKey())
                    .build());

            User savedUser = userService.save(user);
            return new UploadAvatarResponse(savedUser.getId(), newAvatarKeys, savedUser.getUpdatedAt());
        } catch (RuntimeException ex) {
            log.warn("Ошибка при обновлении профиля пользователя, выполняется откат загруженных файлов", ex);
            safeDelete(newAvatarKeys.originalKey());
            safeDelete(newAvatarKeys.profileKey());
            safeDelete(newAvatarKeys.thumbnailKey());
            throw ex;
        }
    }

    public GetAvatarResponse getAvatar(Long userId) {
        throw new UnsupportedOperationException("Метод getAvatar пока не реализован");
    }

    public DeleteAvatarResponse deleteAvatar(Long userId) {
        throw new UnsupportedOperationException("Метод deleteAvatar пока не реализован");
    }

    // Возможные вспомогательные приватные методы

    private byte[] readFileBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (Exception ex) {
            throw new AvatarUploadException("Не удалось прочитать содержимое файла", ex);
        }
    }

    private void uploadFilesWithRollback(
            AvatarFileIdsDto fileIds,
            byte[] originalBytes,
            byte[] profileBytes,
            byte[] thumbnailBytes,
            String contentType
    ) {
        try {
            s3Service.uploadFile(fileIds.originalKey(), originalBytes, contentType);
            s3Service.uploadFile(fileIds.profileKey(), profileBytes, contentType);
            s3Service.uploadFile(fileIds.thumbnailKey(), thumbnailBytes, contentType);
        } catch (RuntimeException ex) {
            log.warn("Ошибка при загрузке новых файлов аватара, выполняется откат", ex);
            safeDelete(fileIds.originalKey());
            safeDelete(fileIds.profileKey());
            safeDelete(fileIds.thumbnailKey());
            throw ex;
        }
    }

    private void deleteOldFiles(AvatarFileIdsDto oldFileIds) {
        if (oldFileIds == null) {
            return;
        }
        safeDelete(oldFileIds.originalKey());
        safeDelete(oldFileIds.profileKey());
        safeDelete(oldFileIds.thumbnailKey());
    }

    private void safeDelete(String key) {
        try {
            s3Service.deleteFile(key);
        } catch (RuntimeException ex) {
            log.warn("Не удалось удалить файл '{}' из хранилища", key, ex);
        }
    }

    private AvatarFileIdsDto extractAvatarFileIds(UserProfileAvatar avatar) {
        if (avatar == null) {
            return null;
        }
        return new AvatarFileIdsDto(
                avatar.getOriginalPath(),
                avatar.getProfilePath(),
                avatar.getThumbnailPath()
        );
    }
}
