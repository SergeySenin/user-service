package io.github.sergeysenin.userservice.exception.type;

import io.github.sergeysenin.userservice.exception.code.ErrorCode;
import io.github.sergeysenin.userservice.exception.type.base.BaseServiceException;

import java.util.Map;

public class UserNotFoundException extends BaseServiceException {

    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }

    public UserNotFoundException(String message) {
        super(ErrorCode.USER_NOT_FOUND, message);
    }

    public UserNotFoundException(String message, Map<String, String> details) {
        super(ErrorCode.USER_NOT_FOUND, message, details);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(ErrorCode.USER_NOT_FOUND, message, cause);
    }

    public UserNotFoundException(String message, Map<String, String> details, Throwable cause) {
        super(ErrorCode.USER_NOT_FOUND, message, details, cause);
    }
}
