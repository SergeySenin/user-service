package io.github.sergeysenin.userservice.exception.type;

import io.github.sergeysenin.userservice.exception.code.ErrorCode;
import io.github.sergeysenin.userservice.exception.type.base.BaseServiceException;

import java.util.Map;

public class FileStorageException extends BaseServiceException {

    public FileStorageException() {
        super(ErrorCode.FILE_STORAGE_ERROR);
    }

    public FileStorageException(String message) {
        super(ErrorCode.FILE_STORAGE_ERROR, message);
    }

    public FileStorageException(String message, Map<String, String> details) {
        super(ErrorCode.FILE_STORAGE_ERROR, message, details);
    }

    public FileStorageException(String message, Throwable cause) {
        super(ErrorCode.FILE_STORAGE_ERROR, message, cause);
    }

    public FileStorageException(String message, Map<String, String> details, Throwable cause) {
        super(ErrorCode.FILE_STORAGE_ERROR, message, details, cause);
    }
}
