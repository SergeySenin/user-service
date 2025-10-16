package io.github.sergeysenin.userservice.service.avatar;

import io.github.sergeysenin.userservice.config.avatar.AvatarProperties;
import io.github.sergeysenin.userservice.dto.avatar.GetAvatarResponse;
import io.github.sergeysenin.userservice.dto.avatar.UploadAvatarResponse;
import io.github.sergeysenin.userservice.dto.avatar.DeleteAvatarResponse;
import io.github.sergeysenin.userservice.dto.avatar.AvatarObjectPathsDto;
import io.github.sergeysenin.userservice.service.avatar.generator.AvatarFileNameGenerator;
import io.github.sergeysenin.userservice.service.resource.ResourceService;
import io.github.sergeysenin.userservice.service.s3.S3Service;
import io.github.sergeysenin.userservice.service.user.UserService;
import io.github.sergeysenin.userservice.validator.resource.ResourceValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

        return new UploadAvatarResponse(userId, new AvatarObjectPathsDto(null, null, null), null);
    }

    public GetAvatarResponse getAvatar(Long userId) {

        return new GetAvatarResponse(userId, new AvatarObjectPathsDto(null, null, null), false);
    }

    public DeleteAvatarResponse deleteAvatar(Long userId) {

        return new DeleteAvatarResponse(userId, false, null);
    }
}

// Написать:
// public UploadAvatarResponse uploadAvatar(Long userId, MultipartFile file) {
// Логика получения и валидации пользователя (userService)
// Логика валидации файла пользователя(resourceValidator)
// Продумать логику работы со старым именем файла пользователя (...)
// Логика генерации нового имени файла пользователя(avatarFileNameGenerator)
// Продумать логику работы изменения файла пользователя (resourceService)
// Продумать логику работы обновления доменной модели и сохранение (...)
// Продумать логику обработки ошибок и логирование (...)
// Формирование ответа (UploadAvatarResponse)
// }
//
// public GetAvatarResponse getAvatar(Long userId) {
// Логика получения и валидации пользователя (userService)
// Продумать логику валидации наличия аватара (AvatarNotFoundException)
// Продумать логику генерации временных URL (s3Service)
// Продумать логику обработки ошибок и логирование
// Формирование ответа (GetAvatarResponse)
// }
//
// public DeleteAvatarResponse deleteAvatar(Long userId) {
// Логика получения и валидации пользователя (userService)
// Продумать логику валидации наличия аватара (AvatarNotFoundException)
// Продумать логику удаления файлов (s3Service)
// Продумать логику обновления доменной модели и сохранение (...)
// Продумать логику обработки ошибок и логирование (...)
// Формирование ответа (DeleteAvatarResponse)
// }
//
// Продумать уровни транзакций;
// Продумать логику обработки ошибок и логирование (рассмотреть возможный откат) (...)
