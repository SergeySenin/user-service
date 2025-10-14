package io.github.sergeysenin.userservice.config.s3;

// Импорты валидации
// Импорты валидации
// Импорты валидации

import org.springframework.boot.context.properties.ConfigurationProperties;
// import org.springframework.boot.context.properties.bind.DefaultValue;
// Возможные аннотации
import org.springframework.validation.annotation.Validated;

// Возможные аннотации

/*
S3Properties — типобезопасная конфигурация.

Значения подставляются из application-local.yaml и application-prod.yaml.
 */
@Validated
@ConfigurationProperties(prefix = "services.s3")
public record S3Properties(

        // Аннотации валидации
        // Поле

        // Аннотации валидации
        // Поле

        // Аннотации валидации
        // Поле

        // Аннотации валидации
        // Поле

        // Аннотации валидации
        // Поле
) {

    public S3Properties(

            // @DefaultValue("") - возможно
            // Поле

            // @DefaultValue("") - возможно
            // Поле

            // @DefaultValue("") - возможно
            // Поле

            // @DefaultValue("") - возможно
            // Поле

            // @DefaultValue("") - возможно
            // Поле
    ) {
        // this. = ;
        // this. = ;
        // this. = ;
        // this. = ;
        // this. = ;
    }

    // Возможные методы класса
}
