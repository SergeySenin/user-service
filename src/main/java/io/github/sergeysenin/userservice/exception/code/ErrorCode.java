package io.github.sergeysenin.userservice.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // → BindException
    BINDING_ERROR("USR-1000", HttpStatus.BAD_REQUEST, "Некорректные входные данные"),
    // → DataValidationException, MethodArgumentNotValidException, javax.validation.ConstraintViolationException
    VALIDATION_FAILED("USR-1001", HttpStatus.UNPROCESSABLE_ENTITY, "Данные не прошли валидацию"),
    // → AvatarUploadException
    AVATAR_UPLOAD_FAILED("USR-1002", HttpStatus.UNPROCESSABLE_ENTITY, "Не удалось загрузить аватар"),

    // → EntityNotFoundException, NoSuchElementException
    ENTITY_NOT_FOUND("USR-2001", HttpStatus.NOT_FOUND, "Сущность не найдена"),
    // → UserNotFoundException
    USER_NOT_FOUND("USR-2002", HttpStatus.NOT_FOUND, "Пользователь не найден"),
    // → AvatarNotFoundException
    AVATAR_NOT_FOUND("USR-2003", HttpStatus.NOT_FOUND, "Аватар не найден"),

    // → org.hibernate.exception.ConstraintViolationException
    CONSTRAINT_VIOLATION("USR-3000", HttpStatus.CONFLICT, "Нарушено ограничение целостности данных"),

    // → AuthenticationEntryPoint
    UNAUTHORIZED("USR-4001", HttpStatus.UNAUTHORIZED, "Требуется аутентификация"),

    // → AccessDeniedHandler
    ACCESS_DENIED("USR-4003", HttpStatus.FORBIDDEN, "Доступ запрещён"),

    // → FileStorageException
    FILE_STORAGE_ERROR("USR-7000", HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка файлового хранилища"),

    // → Exception, RuntimeException
    UNEXPECTED_ERROR("USR-9000", HttpStatus.INTERNAL_SERVER_ERROR, "Необработанная ошибка выполнения");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
