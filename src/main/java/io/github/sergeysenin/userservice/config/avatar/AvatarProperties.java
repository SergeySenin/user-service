package io.github.sergeysenin.userservice.config.avatar;

// Импорты валидации
// Импорты валидации
// Импорты валидации

import org.springframework.boot.context.properties.ConfigurationProperties;
// import org.springframework.boot.context.properties.bind.DefaultValue;
// Возможные аннотации
import org.springframework.validation.annotation.Validated;

// Возможные аннотации

/*
AvatarProperties — типобезопасные настройки.

Значения определены в application.yaml и профилях local/prod.
 */
@Validated
@ConfigurationProperties(prefix = "user.avatar")
public record AvatarProperties(

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

    public AvatarProperties(

            // @DefaultValue("")
            // Поле

            // @DefaultValue("")
            // Поле

            // @DefaultValue("")
            // Поле

            // @DefaultValue("")
            // Поле

            // @DefaultValue("")
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
