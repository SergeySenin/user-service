package io.github.sergeysenin.userservice.exception.type;

import io.github.sergeysenin.userservice.exception.code.ErrorCode;
import io.github.sergeysenin.userservice.exception.type.base.BaseServiceException;

import java.util.Map;

public class CountryNotFoundException extends BaseServiceException {

    public CountryNotFoundException() {
        super(ErrorCode.COUNTRY_NOT_FOUND);
    }

    public CountryNotFoundException(String message) {
        super(ErrorCode.COUNTRY_NOT_FOUND, message);
    }

    public CountryNotFoundException(String message, Map<String, String> details) {
        super(ErrorCode.COUNTRY_NOT_FOUND, message, details);
    }

    public CountryNotFoundException(String message, Throwable cause) {
        super(ErrorCode.COUNTRY_NOT_FOUND, message, cause);
    }

    public CountryNotFoundException(String message, Map<String, String> details, Throwable cause) {
        super(ErrorCode.COUNTRY_NOT_FOUND, message, details, cause);
    }
}
