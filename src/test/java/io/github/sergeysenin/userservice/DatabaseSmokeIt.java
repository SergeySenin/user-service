package io.github.sergeysenin.userservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("DatabaseSmokeIt: контекст поднимается и выполняется простой SQL")
class DatabaseSmokeIt {

    private static final String POSTGRES_IMAGE = "postgres:16.3";
    private static final String DB_NAME = "user_service";
    private static final String DB_USERNAME = "user";
    private static final String DB_PASSWORD = "password";

    private static final DockerImageName POSTGRES_DOCKER_IMAGE = DockerImageName.parse(POSTGRES_IMAGE);

    @Container
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>(POSTGRES_DOCKER_IMAGE)
            .withDatabaseName(DB_NAME)
            .withUsername(DB_USERNAME)
            .withPassword(DB_PASSWORD);

    @DynamicPropertySource
    static void registerDataSourceProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        dynamicPropertyRegistry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        dynamicPropertyRegistry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
    }

    @Autowired
    DataSource dataSource;

    @Test
    @SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
    @DisplayName("должен выполнять 'select 1' при корректной конфигурации DataSource")
    void shouldExecuteSelectOneWhenDataSourceConfigured() throws Exception {
        var sql = "select 1";

        try (
                var connection = dataSource.getConnection();
                var statement = connection.createStatement();
                var resultSet = statement.executeQuery(sql)
        ) {
            assertAll("Результат простого SQL-запроса должен быть корректен",
                    () -> assertTrue(resultSet.next(), "Должна вернуться как минимум одна строка"),
                    () -> assertEquals(1, resultSet.getInt(1), "Первое значение должно быть 1")
            );
        }
    }
}
