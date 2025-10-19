package io.github.sergeysenin.userservice.exception.dto;

import io.github.sergeysenin.userservice.exception.code.ErrorCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ErrorResponse")
class ErrorResponseTest {

    private static final String VALID_CODE = "USR-9999";
    private static final String CUSTOM_MESSAGE = "Произошла кастомная ошибка";
    private static final String DETAILS_KEY = "field";
    private static final String DETAILS_VALUE = "invalid";
    private static final String BLANK_MESSAGE = "   ";

    @Nested
    @DisplayName("Позитивные сценарии конструктора")
    class ConstructorPositiveTests {

        @Test
        @DisplayName("должен создавать неизменяемую копию деталей при передаче непустой коллекции")
        void shouldCreateImmutableCopyWhenDetailsProvided() {
            Map<String, String> sourceDetails = createDetails();

            ErrorResponse sut = createSut(VALID_CODE, CUSTOM_MESSAGE, Instant.now(), sourceDetails);

            sourceDetails.put("other", "value");

            assertAll("Проверка неизменности деталей",
                    () -> assertEquals(Map.of(DETAILS_KEY, DETAILS_VALUE), sut.details(),
                            "Детали должны копироваться при создании ответа"),
                    () -> assertThrows(UnsupportedOperationException.class,
                            () -> sut.details().put("extra", "value"),
                            "Коллекция деталей должна быть неизменяемой"));
        }

        @Test
        @DisplayName("должен использовать переданный timestamp, если он не равен null")
        void shouldUseProvidedTimestampWhenNotNull() {
            Instant expectedTimestamp = Instant.parse("2025-01-01T10:15:30Z");

            ErrorResponse sut = createSut(VALID_CODE, CUSTOM_MESSAGE, expectedTimestamp, createDetails());

            assertAll("Проверка переданного timestamp",
                    () -> assertEquals(expectedTimestamp, sut.timestamp(),
                            "Переданный timestamp должен сохраниться без изменений"),
                    () -> assertEquals(Map.of(DETAILS_KEY, DETAILS_VALUE), sut.details(),
                            "Детали должны соответствовать исходным значениям"));
        }

        @Test
        @DisplayName("должен устанавливать текущий timestamp, если передан null")
        void shouldSetCurrentTimestampWhenTimestampIsNull() {
            Instant lowerBound = Instant.now();

            ErrorResponse sut = createSut(VALID_CODE, CUSTOM_MESSAGE, null, createDetails());

            Instant upperBound = Instant.now();

            assertResponse(
                    sut,
                    VALID_CODE,
                    CUSTOM_MESSAGE,
                    lowerBound,
                    upperBound,
                    Map.of(DETAILS_KEY, DETAILS_VALUE)
            );
        }

        @Test
        @DisplayName("должен возвращать пустые детали, если переданы null")
        void shouldReturnEmptyDetailsWhenDetailsNull() {
            ErrorResponse sut = createSut(VALID_CODE, CUSTOM_MESSAGE, Instant.now(), null);

            assertAll("Проверка пустых деталей при null",
                    () -> assertTrue(sut.details().isEmpty(),
                            "Детали должны быть пустыми при передаче null"),
                    () -> assertThrows(UnsupportedOperationException.class,
                            () -> sut.details().put(DETAILS_KEY, DETAILS_VALUE),
                            "Пустая коллекция деталей должна быть неизменяемой"));
        }

        @Test
        @DisplayName("должен возвращать пустые детали, если передана пустая коллекция")
        void shouldReturnEmptyDetailsWhenDetailsEmpty() {
            ErrorResponse sut = createSut(VALID_CODE, CUSTOM_MESSAGE, Instant.now(), Map.of());

            assertAll("Проверка пустых деталей при пустой коллекции",
                    () -> assertTrue(sut.details().isEmpty(),
                            "Детали должны быть пустыми при передаче пустой коллекции"),
                    () -> assertThrows(UnsupportedOperationException.class,
                            () -> sut.details().put(DETAILS_KEY, DETAILS_VALUE),
                            "Пустая коллекция деталей должна быть неизменяемой"));
        }
    }

    @Nested
    @DisplayName("Проверка валидации конструктора")
    class ConstructorValidationTests {

        @Test
        @DisplayName("должен выбрасывать NullPointerException, если код равен null")
        void shouldThrowNullPointerExceptionWhenCodeIsNull() {
            assertThrows(NullPointerException.class,
                    () -> new ErrorResponse(null, CUSTOM_MESSAGE, Instant.now(), createDetails()),
                    "Конструктор должен проверять код на null");
        }

        @Test
        @DisplayName("должен выбрасывать NullPointerException, если сообщение равно null")
        void shouldThrowNullPointerExceptionWhenMessageIsNull() {
            assertThrows(NullPointerException.class,
                    () -> new ErrorResponse(VALID_CODE, null, Instant.now(), createDetails()),
                    "Конструктор должен проверять сообщение на null");
        }
    }

    @Nested
    @DisplayName("Фабричный метод of")
    class FactoryMethodTests {

        private static final ErrorCode TEST_ERROR_CODE = ErrorCode.BINDING_ERROR;

        @Test
        @DisplayName("должен использовать переданное сообщение, если оно непустое")
        void shouldBuildResponseWithProvidedMessageWhenMessageNotBlank() {
            Instant lowerBound = Instant.now();

            ErrorResponse sut = createSutFromFactory(TEST_ERROR_CODE, CUSTOM_MESSAGE, createDetails());

            Instant upperBound = Instant.now();

            assertResponse(
                    sut,
                    TEST_ERROR_CODE.getCode(),
                    CUSTOM_MESSAGE,
                    lowerBound,
                    upperBound,
                    Map.of(DETAILS_KEY, DETAILS_VALUE)
            );
        }

        @Test
        @DisplayName("должен использовать значение по умолчанию, если сообщение равно null")
        void shouldUseDefaultMessageWhenMessageIsNull() {
            Instant lowerBound = Instant.now();

            ErrorResponse sut = createSutFromFactory(TEST_ERROR_CODE, null, createDetails());

            Instant upperBound = Instant.now();

            assertResponse(
                    sut,
                    TEST_ERROR_CODE.getCode(),
                    TEST_ERROR_CODE.getDefaultMessage(),
                    lowerBound,
                    upperBound,
                    Map.of(DETAILS_KEY, DETAILS_VALUE)
            );
        }

        @Test
        @DisplayName("должен использовать значение по умолчанию, если сообщение состоит из пробелов")
        void shouldUseDefaultMessageWhenMessageIsBlank() {
            Instant lowerBound = Instant.now();

            ErrorResponse sut = createSutFromFactory(TEST_ERROR_CODE, BLANK_MESSAGE, createDetails());

            Instant upperBound = Instant.now();

            assertResponse(
                    sut,
                    TEST_ERROR_CODE.getCode(),
                    TEST_ERROR_CODE.getDefaultMessage(),
                    lowerBound,
                    upperBound,
                    Map.of(DETAILS_KEY, DETAILS_VALUE)
            );
        }

        @Test
        @DisplayName("должен возвращать пустые детали, если аргумент details равен null")
        void shouldReturnEmptyDetailsWhenDetailsArgumentIsNull() {
            ErrorResponse sut = createSutFromFactory(TEST_ERROR_CODE, CUSTOM_MESSAGE, null);

            assertAll("Проверка пустых деталей в фабричном методе",
                    () -> assertTrue(sut.details().isEmpty(),
                            "Детали должны быть пустыми при передаче null в фабричный метод"),
                    () -> assertThrows(UnsupportedOperationException.class,
                            () -> sut.details().put(DETAILS_KEY, DETAILS_VALUE),
                            "Пустая коллекция деталей должна быть неизменяемой"));
        }

        @Test
        @DisplayName("должен выбрасывать NullPointerException, если errorCode равен null")
        void shouldThrowNullPointerExceptionWhenErrorCodeIsNull() {
            assertThrows(NullPointerException.class,
                    () -> createSutFromFactory(null, CUSTOM_MESSAGE, createDetails()),
                    "Фабричный метод должен проверять errorCode на null");
        }
    }

    private Map<String, String> createDetails() {
        Map<String, String> mutableDetails = new HashMap<>();
        mutableDetails.put(DETAILS_KEY, DETAILS_VALUE);
        return mutableDetails;
    }

    private ErrorResponse createSut(String code, String message, Instant timestamp, Map<String, String> details) {
        return new ErrorResponse(code, message, timestamp, details);
    }

    private ErrorResponse createSutFromFactory(ErrorCode errorCode, String message, Map<String, String> details) {
        return ErrorResponse.of(errorCode, message, details);
    }

    private void assertResponse(
            ErrorResponse actualResponse,
            String expectedCode,
            String expectedMessage,
            Instant lowerTimestampBound,
            Instant upperTimestampBound,
            Map<String, String> expectedDetails
    ) {
        assertAll("Проверка содержимого ErrorResponse",
                () -> assertEquals(expectedCode, actualResponse.code(),
                        "Код ошибки должен совпадать с ожидаемым"),
                () -> assertEquals(expectedMessage, actualResponse.message(),
                        "Сообщение должно совпадать с ожидаемым"),
                () -> {
                    Instant actualTimestamp = actualResponse.timestamp();
                    assertNotNull(actualTimestamp, "Timestamp не должен быть null");
                    assertTrue(!actualTimestamp.isBefore(lowerTimestampBound) &&
                                    !actualTimestamp.isAfter(upperTimestampBound),
                            "Timestamp должен находиться в ожидаемом диапазоне");
                },
                () -> assertEquals(expectedDetails, actualResponse.details(),
                        "Детали должны совпадать с ожидаемыми"));
    }
}
