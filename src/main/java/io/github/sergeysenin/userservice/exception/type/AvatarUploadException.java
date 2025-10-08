package io.github.sergeysenin.userservice.exception.type;

import io.github.sergeysenin.userservice.exception.code.ErrorCode;
import io.github.sergeysenin.userservice.exception.type.base.BaseServiceException;

import java.util.Map;

public class AvatarUploadException extends BaseServiceException {

    public AvatarUploadException() {
        super(ErrorCode.AVATAR_UPLOAD_FAILED);
    }

    public AvatarUploadException(String message) {
        super(ErrorCode.AVATAR_UPLOAD_FAILED, message);
    }

    public AvatarUploadException(String message, Map<String, String> details) {
        super(ErrorCode.AVATAR_UPLOAD_FAILED, message, details);
    }

    public AvatarUploadException(String message, Throwable cause) {
        super(ErrorCode.AVATAR_UPLOAD_FAILED, message, cause);
    }

    public AvatarUploadException(String message, Map<String, String> details, Throwable cause) {
        super(ErrorCode.AVATAR_UPLOAD_FAILED, message, details, cause);
    }
}
