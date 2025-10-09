package io.github.sergeysenin.userservice.exception.handler;

import io.github.sergeysenin.userservice.exception.code.ErrorCode;
import io.github.sergeysenin.userservice.exception.dto.ErrorResponse;
import io.github.sergeysenin.userservice.exception.type.base.BaseServiceException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        Map<String, String> details = extractBindingResultDetails(exception.getBindingResult());
        return warnAndBuild(
                ErrorCode.VALIDATION_FAILED,
                "Ошибка валидации аргументов контроллера: details={}, message={}",
                details,
                exception
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        Map<String, String> details = extractConstraintViolationDetails(exception);
        return warnAndBuild(
                ErrorCode.VALIDATION_FAILED,
                "Нарушение ограничений данных: details={}, message={}",
                details,
                exception
        );
    }

    @ExceptionHandler(org.hibernate.exception.ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseConstraintViolation(
            org.hibernate.exception.ConstraintViolationException exception
    ) {
        Map<String, String> details = new LinkedHashMap<>();
        if (exception.getConstraintName() != null && !exception.getConstraintName().isBlank()) {
            details.put("constraint", exception.getConstraintName());
        }
        log.warn(
                "Нарушение ограничений БД: constraint={}, message={}",
                exception.getConstraintName(),
                exception.getMessage()
        );
        return buildResponse(ErrorCode.CONSTRAINT_VIOLATION, null, details.isEmpty() ? null : details);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException exception) {
        Map<String, String> details = extractBindingResultDetails(exception.getBindingResult());
        return warnAndBuild(
                ErrorCode.BINDING_ERROR,
                "Ошибка биндинга данных: details={}, message={}",
                details,
                exception
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException exception) {
        log.warn("Сущность не найдена: {}", exception.getMessage());
        return buildResponse(ErrorCode.ENTITY_NOT_FOUND, exception.getMessage(), null);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElement(NoSuchElementException exception) {
        log.warn("Элемент не найден: {}", exception.getMessage());
        return buildResponse(ErrorCode.ENTITY_NOT_FOUND, exception.getMessage(), null);
    }

    @ExceptionHandler(BaseServiceException.class)
    public ResponseEntity<ErrorResponse> handleBaseServiceException(BaseServiceException exception) {
        ErrorResponse response = exception.toErrorResponse();
        log.warn("Бизнес-ошибка: {}", response);
        return ResponseEntity.status(exception.getErrorCode().getHttpStatus()).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException exception) {
        log.error("Необработанная ошибка выполнения", exception);
        return buildResponse(ErrorCode.UNEXPECTED_ERROR, null, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        log.error("Неизвестная ошибка", exception);
        return buildResponse(ErrorCode.UNEXPECTED_ERROR, null, null);
    }

    private ResponseEntity<ErrorResponse> warnAndBuild(
            ErrorCode errorCode,
            String logTemplate,
            Map<String, String> details,
            Exception exception
    ) {
        log.warn(logTemplate, details, exception.getMessage());
        return buildResponse(errorCode, null, details);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            ErrorCode errorCode,
            String message,
            Map<String, String> details
    ) {
        ErrorResponse response = ErrorResponse.of(errorCode, message, details);
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    private Map<String, String> extractBindingResultDetails(BindingResult bindingResult) {
        Map<String, String> details = new LinkedHashMap<>();
        for (ObjectError error : bindingResult.getAllErrors()) {
            String key = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
            details.put(key, resolveBindingMessage(error.getDefaultMessage(), error.getCode()));
        }
        return details;
    }

    private Map<String, String> extractConstraintViolationDetails(ConstraintViolationException exception) {
        return exception.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        GlobalExceptionHandler::resolveConstraintMessage,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    private static String resolveBindingMessage(String message, String fallback) {
        if (message != null && !message.isBlank()) {
            return message;
        }
        return fallback != null ? fallback : "Validation error";
    }

    private static String resolveConstraintMessage(ConstraintViolation<?> violation) {
        String message = violation.getMessage();
        if (message != null && !message.isBlank()) {
            return message;
        }
        return violation.getMessageTemplate();
    }
}
