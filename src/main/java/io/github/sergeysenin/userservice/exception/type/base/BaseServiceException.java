package io.github.sergeysenin.userservice.exception.type.base;

import io.github.sergeysenin.userservice.exception.code.ErrorCode;
import io.github.sergeysenin.userservice.exception.dto.ErrorResponse;

import lombok.Getter;
import lombok.ToString;

import java.io.Serial;
import java.util.Map;
import java.util.Objects;

@Getter
@ToString(callSuper = true)
public abstract class BaseServiceException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;
    private final Map<String, String> details;

    protected BaseServiceException(ErrorCode errorCode) {
        this(errorCode, null, null, null);
    }

    protected BaseServiceException(ErrorCode errorCode, String message) {
        this(errorCode, message, null, null);
    }

    protected BaseServiceException(ErrorCode errorCode, String message, Map<String, String> details) {
        this(errorCode, message, details, null);
    }

    protected BaseServiceException(ErrorCode errorCode, String message, Throwable cause) {
        this(errorCode, message, null, cause);
    }

    protected BaseServiceException(ErrorCode errorCode, String message, Map<String, String> details, Throwable cause) {
        super((message == null || message.isBlank()) ? errorCode.getDefaultMessage() : message, cause);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode не может быть null");
        this.details = validateAndCopyDetails(details);
    }

    private static Map<String, String> validateAndCopyDetails(Map<String, String> details) {
        if (details == null || details.isEmpty()) {
            return Map.of();
        }
        for (Map.Entry<String, String> entry : details.entrySet()) {
            if (entry.getKey() == null) {
                throw new NullPointerException("details содержит null-ключ");
            }
            if (entry.getValue() == null) {
                throw new NullPointerException(
                        "details содержит null-значение для ключа '" + entry.getKey() + "'"
                );
            }
        }
        return Map.copyOf(details);
    }

    public ErrorResponse toErrorResponse() {
        return ErrorResponse.of(errorCode, getMessage(), details);
    }
}
