package io.github.sergeysenin.userservice.controller.user;

import io.github.sergeysenin.userservice.dto.user.CreateUserRequest;
import io.github.sergeysenin.userservice.dto.user.UpdateUserRequest;
import io.github.sergeysenin.userservice.dto.user.UserResponse;
import io.github.sergeysenin.userservice.mapper.user.UserMapper;
import io.github.sergeysenin.userservice.service.user.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(
        name = "Пользователи",
        description = "CRUD-операции с профилем пользователя"
)
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Создать профиль пользователя",
            description = "Создаёт профиль без управления паролем"
    )
    @PreAuthorize("@userSecurity.isAdmin(authentication)")
    public UserResponse createUser(
            @Valid
            @RequestBody
            CreateUserRequest request
    ) {
        log.info("Запрос на создание пользователя: username={}, email={}, phone={}",
                request.username(), request.email(), request.phone());
        return userMapper.toResponse(userService.createUser(request));
    }

    @PutMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Обновить профиль пользователя",
            description = "Обновляет атрибуты профиля без пароля"
    )
    @PreAuthorize("@userSecurity.canAccessUserResource(#userId, authentication)")
    public UserResponse updateUser(
            @PathVariable("userId")
            @NotNull
            @Positive
            Long userId,

            @Valid
            @RequestBody
            UpdateUserRequest request
    ) {
        log.info("Запрос на обновление пользователя: id={}", userId);
        return userMapper.toResponse(userService.updateUser(userId, request));
    }

    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Получить профиль пользователя",
            description = "Возвращает профиль по идентификатору"
    )
    @PreAuthorize("@userSecurity.canAccessUserResource(#userId, authentication)")
    public UserResponse getUserById(
            @PathVariable("userId")
            @NotNull
            @Positive
            Long userId
    ) {
        log.info("Запрос на получение пользователя: id={}", userId);
        return userMapper.toResponse(userService.getUserByIdOrThrow(userId));
    }
}
