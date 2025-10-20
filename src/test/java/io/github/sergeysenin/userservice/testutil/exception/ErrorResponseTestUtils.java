package io.github.sergeysenin.userservice.testutil.exception;

import io.github.sergeysenin.userservice.exception.dto.ErrorResponse;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Утильные методы и проверки для тестов {@link ErrorResponse}.
 */
public final class ErrorResponseTestUtils {

    private ErrorResponseTestUtils() {
    }

    // Создаёт изменяемый словарь деталей ошибки с заданной парой ключ/значение.
    public static Map<String, String> createDetails(String key, String value) {
        Map<String, String> details = new HashMap<>();
        details.put(key, value);
        return details;
    }

    // Проверяет содержимое ErrorResponse и диапазон временной метки.
    public static void assertResponse(
            ErrorResponse response,
            String expectedCode,
            String expectedMessage,
            Instant lowerTimestampBound,
            Instant upperTimestampBound,
            Map<String, String> expectedDetails
    ) {
        assertNotNull(response, "Ответ не должен быть null");
        assertAll("Проверка содержимого ErrorResponse",
                () -> assertEquals(expectedCode, response.code(),
                        "Код ошибки должен совпадать с ожидаемым"),
                () -> assertEquals(expectedMessage, response.message(),
                        "Сообщение должно совпадать с ожидаемым"),
                () -> {
                    Instant actualTimestamp = response.timestamp();
                    assertNotNull(actualTimestamp, "Timestamp не должен быть null");
                    assertAll("Проверка диапазона timestamp",
                            () -> assertTrue(!actualTimestamp.isBefore(lowerTimestampBound),
                                    "Timestamp не должен быть раньше нижней границы"),
                            () -> assertTrue(!actualTimestamp.isAfter(upperTimestampBound),
                                    "Timestamp не должен быть позже верхней границы"));
                },
                () -> assertEquals(expectedDetails, response.details(),
                        "Детали должны совпадать с ожидаемыми"));
    }
}
