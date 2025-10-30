package io.github.sergeysenin.userservice.config.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;
import java.util.function.Supplier;

@Configuration
public class IdentifierConfig {

    @Bean
    public Supplier<UUID> avatarUuidSupplier() {
        return UUID::randomUUID;
    }
}
