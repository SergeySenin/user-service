package io.github.sergeysenin.userservice.service.avatar;

import io.github.sergeysenin.userservice.config.avatar.AvatarProperties;
import io.github.sergeysenin.userservice.dto.avatar.DeleteAvatarResponse;
import io.github.sergeysenin.userservice.dto.avatar.GetAvatarResponse;
import io.github.sergeysenin.userservice.dto.avatar.UploadAvatarResponse;
import io.github.sergeysenin.userservice.service.avatar.generator.AvatarFileNameGenerator;
import io.github.sergeysenin.userservice.service.resource.ResourceService;
import io.github.sergeysenin.userservice.service.s3.S3Service;
import io.github.sergeysenin.userservice.service.user.UserService;
import io.github.sergeysenin.userservice.validator.resource.ResourceValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    public UploadAvatarResponse uploadAvatar(Long userId, MultipartFile file) {
        // Реализация метода
        return null;
    }

    public GetAvatarResponse getAvatar(Long userId) {
        // Реализация метода
        return null;
    }

    public DeleteAvatarResponse deleteAvatar(Long userId) {
        // Реализация метода
        return null;
    }

    // Возможные вспомогательные приватные методы
}
