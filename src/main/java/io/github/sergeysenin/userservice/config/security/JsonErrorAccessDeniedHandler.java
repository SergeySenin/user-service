package io.github.sergeysenin.userservice.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.sergeysenin.userservice.exception.code.ErrorCode;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

public class JsonErrorAccessDeniedHandler extends AbstractJsonSecurityHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(JsonErrorAccessDeniedHandler.class);

    public JsonErrorAccessDeniedHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    @SuppressWarnings("RedundantThrows")
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        String requestUri = request.getRequestURI();
        log.warn("Доступ запрещен к {}: {}", requestUri, accessDeniedException.getMessage());

        writeResponse(response, ErrorCode.ACCESS_DENIED, requestUri);
    }
}
