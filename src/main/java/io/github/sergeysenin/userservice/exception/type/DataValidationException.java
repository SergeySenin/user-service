package io.github.sergeysenin.userservice.exception.type;

import io.github.sergeysenin.userservice.exception.code.ErrorCode;
import io.github.sergeysenin.userservice.exception.type.base.BaseServiceException;

import java.util.Map;

public class DataValidationException extends BaseServiceException {

    public DataValidationException() {
        super(ErrorCode.VALIDATION_FAILED);
    }

    public DataValidationException(String message) {
        super(ErrorCode.VALIDATION_FAILED, message);
    }

    public DataValidationException(String message, Map<String, String> details) {
        super(ErrorCode.VALIDATION_FAILED, message, details);
    }

    public DataValidationException(String message, Throwable cause) {
        super(ErrorCode.VALIDATION_FAILED, message, cause);
    }

    public DataValidationException(String message, Map<String, String> details, Throwable cause) {
        super(ErrorCode.VALIDATION_FAILED, message, details, cause);
    }
}
