package io.github.sergeysenin.userservice.controller.user;

import io.github.sergeysenin.userservice.dto.avatar.DeleteAvatarResponse;
import io.github.sergeysenin.userservice.dto.avatar.GetAvatarResponse;
import io.github.sergeysenin.userservice.dto.avatar.UploadAvatarResponse;
import io.github.sergeysenin.userservice.service.avatar.AvatarService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Validated
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(
        name = "Аватары пользователей",
        description = "Операции загрузки, получения, удаления аватаров пользователей"
)
public class UserAvatarController {

    private final AvatarService avatarService;

    @PostMapping("/{userId}/avatar")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Загрузить новый аватар",
            description = "Принимает файл изображения, сохраняет все версии в S3 и возвращает ключи объектов"
    )
    @PreAuthorize("@userSecurity.canAccessUserResource(#userId, authentication)")
    public UploadAvatarResponse uploadAvatar(
            @PathVariable("userId")
            @NotNull
            @Positive
            Long userId,

            @RequestParam("file")
            MultipartFile file
    ) {
        String fileName = file != null ? file.getOriginalFilename() : null;
        log.info("Запрос на загрузку аватара: userId={}, fileName={}", userId, fileName);
        return avatarService.uploadAvatar(userId, file);
    }

    @GetMapping("/{userId}/avatar")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Получить ссылки на аватар",
            description = "Возвращает предварительно подписанные URL для всех доступных версий аватара"
    )
    @PreAuthorize("@userSecurity.canAccessUserResource(#userId, authentication)")
    public GetAvatarResponse getAvatar(
            @PathVariable("userId")
            @NotNull
            @Positive
            Long userId
    ) {
        log.info("Запрос на получение аватара: userId={}", userId);
        return avatarService.getAvatar(userId);
    }

    @DeleteMapping("/{userId}/avatar")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Удалить аватар",
            description = "Удаляет все версии аватара пользователя из S3 и базы данных"
    )
    @PreAuthorize("@userSecurity.canAccessUserResource(#userId, authentication)")
    public DeleteAvatarResponse deleteAvatar(
            @PathVariable("userId")
            @NotNull
            @Positive
            Long userId
    ) {
        log.info("Запрос на удаление аватара: userId={}", userId);
        return avatarService.deleteAvatar(userId);
    }
}
