package io.github.sergeysenin.userservice.exception.dto;

import io.github.sergeysenin.userservice.exception.code.ErrorCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

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

    private static Stream<Map<String, String>> emptyDetails() {
        return Stream.of(Map.of());
    }

    @Nested
    @DisplayName("Позитивные сценарии конструктора")
    class ConstructorPositiveTests {

        @Test
        @DisplayName("должен создавать неизменяемую копию деталей при передаче непустой коллекции")
        void shouldCreateImmutableCopyWhenDetailsProvided() {
            Map<String, String> sourceDetails = createDetails();

            ErrorResponse sut = createSut(VALID_CODE, CUSTOM_MESSAGE, Instant.now(), sourceDetails);

            sourceDetails.put("other", "value");

            assertAll("Проверка копирования и неизменности деталей",
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

            assertResponse(sut, VALID_CODE, CUSTOM_MESSAGE, lowerBound, upperBound, Map.of(DETAILS_KEY, DETAILS_VALUE));
        }

        @DisplayName("должен возвращать пустые детали, если переданы null или пустые")
        @ParameterizedTest(name = "значение details: {0}")
        @NullSource
        @MethodSource("io.github.sergeysenin.userservice.exception.dto.ErrorResponseTest#emptyDetails")
        void shouldReturnEmptyDetailsWhenDetailsNullOrEmpty(Map<String, String> details) {
            ErrorResponse sut = createSut(VALID_CODE, CUSTOM_MESSAGE, Instant.now(), details);

            assertAll("Проверка пустых деталей при отсутствии данных",
                    () -> assertTrue(sut.details().isEmpty(),
                            "Детали должны быть пустыми при передаче null или пустой коллекции"),
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
            NullPointerException exception = assertThrows(NullPointerException.class,
                    () -> new ErrorResponse(null, CUSTOM_MESSAGE, Instant.now(), createDetails()),
                    "Конструктор должен проверять код на null");

            assertAll("Проверка данных исключения",
                    () -> assertEquals("code не может быть null", exception.getMessage(),
                            "Сообщение исключения должно содержать причину отсутствия кода"));
        }

        @Test
        @DisplayName("должен выбрасывать NullPointerException, если сообщение равно null")
        void shouldThrowNullPointerExceptionWhenMessageIsNull() {
            NullPointerException exception = assertThrows(NullPointerException.class,
                    () -> new ErrorResponse(VALID_CODE, null, Instant.now(), createDetails()),
                    "Конструктор должен проверять сообщение на null");

            assertAll("Проверка данных исключения",
                    () -> assertEquals("message не может быть null", exception.getMessage(),
                            "Сообщение исключения должно содержать причину отсутствия текста"));
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

        @DisplayName("должен использовать значение по умолчанию, если сообщение равно null или пустое")
        @ParameterizedTest(name = "значение сообщения: {0}")
        @NullSource
        @ValueSource(strings = {"   "})
        void shouldUseDefaultMessageWhenMessageNullOrBlank(String message) {
            Instant lowerBound = Instant.now();

            ErrorResponse sut = createSutFromFactory(TEST_ERROR_CODE, message, createDetails());

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

            assertAll("Проверка пустых деталей при отсутствии данных",
                    () -> assertTrue(sut.details().isEmpty(),
                            "Детали должны быть пустыми при передаче null в фабричный метод"),
                    () -> assertThrows(UnsupportedOperationException.class,
                            () -> sut.details().put(DETAILS_KEY, DETAILS_VALUE),
                            "Пустая коллекция деталей должна быть неизменяемой"));
        }

        @Test
        @DisplayName("должен выбрасывать NullPointerException, если errorCode равен null")
        void shouldThrowNullPointerExceptionWhenErrorCodeIsNull() {
            NullPointerException exception = assertThrows(NullPointerException.class,
                    () -> createSutFromFactory(null, CUSTOM_MESSAGE, createDetails()),
                    "Фабричный метод должен проверять errorCode на null");

            assertAll("Проверка данных исключения",
                    () -> assertEquals("errorCode не может быть null", exception.getMessage(),
                            "Сообщение исключения должно содержать причину отсутствия errorCode"));
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
                    assertAll("Проверка диапазона timestamp",
                            () -> assertTrue(!actualTimestamp.isBefore(lowerTimestampBound),
                                    "Timestamp не должен быть раньше нижней границы"),
                            () -> assertTrue(!actualTimestamp.isAfter(upperTimestampBound),
                                    "Timestamp не должен быть позже верхней границы"));
                },
                () -> assertEquals(expectedDetails, actualResponse.details(),
                        "Детали должны совпадать с ожидаемыми"));
    }
}
