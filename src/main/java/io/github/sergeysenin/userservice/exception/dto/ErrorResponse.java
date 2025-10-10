package io.github.sergeysenin.userservice.exception.dto;

import io.github.sergeysenin.userservice.exception.code.ErrorCode;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record ErrorResponse(
        String code,
        String message,
        Instant timestamp,
        Map<String, String> details
) {

    private static final Map<String, String> EMPTY_DETAILS = Map.of();

    public ErrorResponse {
        Objects.requireNonNull(code, "code не может быть null");
        Objects.requireNonNull(message, "message не может быть null");
        timestamp = Objects.requireNonNullElseGet(timestamp, Instant::now);
        details = (details == null || details.isEmpty()) ? EMPTY_DETAILS : Map.copyOf(details);
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, Map<String, String> details) {
        Objects.requireNonNull(errorCode, "errorCode не может быть null");
        String resolvedMessage = (message == null || message.isBlank()) ?
                errorCode.getDefaultMessage() :
                message;
        return new ErrorResponse(errorCode.getCode(), resolvedMessage, null, details);
    }
}
