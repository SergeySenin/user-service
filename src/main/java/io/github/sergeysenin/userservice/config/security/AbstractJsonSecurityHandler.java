package io.github.sergeysenin.userservice.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.sergeysenin.userservice.exception.code.ErrorCode;
import io.github.sergeysenin.userservice.exception.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

abstract class AbstractJsonSecurityHandler {

    private final ObjectMapper objectMapper;

    protected AbstractJsonSecurityHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    protected void writeResponse(
            HttpServletResponse response,
            ErrorCode errorCode,
            String requestUri
    ) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Map<String, String> details = requestUri == null ?
                Map.of() :
                Map.of("path", requestUri);

        ErrorResponse body = ErrorResponse.of(errorCode, null, details);
        objectMapper.writeValue(response.getWriter(), body);
    }
}
