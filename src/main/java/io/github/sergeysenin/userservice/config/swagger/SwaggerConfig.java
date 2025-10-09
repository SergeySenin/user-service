package io.github.sergeysenin.userservice.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.servers.Server;

import org.springdoc.core.models.GroupedOpenApi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.List;

@Configuration
public class SwaggerConfig {

    private final String applicationName;
    private final String applicationVersion;
    private final String contextPath;

    public SwaggerConfig(
            @Value("${spring.application.name:user-service}") String applicationName,
            @Value("${info.app.version:0.0.1-SNAPSHOT}") String applicationVersion,
            @Value("${server.servlet.context-path:}") String contextPath
    ) {
        this.applicationName = applicationName;
        this.applicationVersion = applicationVersion;
        this.contextPath = contextPath;
    }

    @Bean
    public OpenAPI userServiceOpenApi() {
        return new OpenAPI()
                .info(buildApiInfo())
                .servers(List.of(buildPrimaryServer()))
                .externalDocs(buildExternalDocumentation());
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/api/v1/**")
                .build();
    }

    private Info buildApiInfo() {
        return new Info()
                .title(applicationName + " API")
                .version(applicationVersion)
                .description("Заглушка описания API пользовательского сервиса.")
                .contact(new Contact()
                        .name("Команда пользовательского сервиса")
                        .email("team@example.com")
                        .url("https://example.com"))
                .license(new License()
                        .name("Пример лицензии")
                        .url("https://example.com/license"));
    }

    private Server buildPrimaryServer() {
        String serverUrl = StringUtils.hasText(contextPath) ? contextPath : "/";

        return new Server()
                .url(serverUrl)
                .description("Основной сервер пользовательского сервиса");
    }

    private ExternalDocumentation buildExternalDocumentation() {
        return new ExternalDocumentation()
                .description("Внешняя документация пользовательского сервиса")
                .url("https://example.com/docs");
    }
}
