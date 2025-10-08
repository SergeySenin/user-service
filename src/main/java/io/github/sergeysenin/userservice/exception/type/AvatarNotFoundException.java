package io.github.sergeysenin.userservice.exception.type;

import io.github.sergeysenin.userservice.exception.code.ErrorCode;
import io.github.sergeysenin.userservice.exception.type.base.BaseServiceException;

import java.util.Map;

public class AvatarNotFoundException extends BaseServiceException {

    public AvatarNotFoundException() {
        super(ErrorCode.AVATAR_NOT_FOUND);
    }

    public AvatarNotFoundException(String message) {
        super(ErrorCode.AVATAR_NOT_FOUND, message);
    }

    public AvatarNotFoundException(String message, Map<String, String> details) {
        super(ErrorCode.AVATAR_NOT_FOUND, message, details);
    }

    public AvatarNotFoundException(String message, Throwable cause) {
        super(ErrorCode.AVATAR_NOT_FOUND, message, cause);
    }

    public AvatarNotFoundException(String message, Map<String, String> details, Throwable cause) {
        super(ErrorCode.AVATAR_NOT_FOUND, message, details, cause);
    }
}
