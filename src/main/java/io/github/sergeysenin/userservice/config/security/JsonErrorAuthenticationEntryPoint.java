package io.github.sergeysenin.userservice.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.sergeysenin.userservice.exception.code.ErrorCode;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class JsonErrorAuthenticationEntryPoint extends AbstractJsonSecurityHandler implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(JsonErrorAuthenticationEntryPoint.class);

    public JsonErrorAuthenticationEntryPoint(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    @SuppressWarnings("RedundantThrows")
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        String requestUri = request.getRequestURI();
        log.warn("Неавторизованный доступ к {}: {}", requestUri, authException.getMessage());

        writeResponse(response, ErrorCode.UNAUTHORIZED, requestUri);
    }
}
