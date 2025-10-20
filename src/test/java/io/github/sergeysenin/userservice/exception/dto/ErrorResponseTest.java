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
import java.util.Map;
import java.util.stream.Stream;

import static io.github.sergeysenin.userservice.testutil.exception.ErrorResponseTestUtils.assertResponse;
import static io.github.sergeysenin.userservice.testutil.exception.ErrorResponseTestUtils.createDetails;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
            Map<String, String> sourceDetails = createDetails(DETAILS_KEY, DETAILS_VALUE);

            ErrorResponse sut = new ErrorResponse(VALID_CODE, CUSTOM_MESSAGE, Instant.now(), sourceDetails);

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

            ErrorResponse sut = new ErrorResponse(
                    VALID_CODE,
                    CUSTOM_MESSAGE,
                    expectedTimestamp,
                    createDetails(DETAILS_KEY, DETAILS_VALUE)
            );

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

            ErrorResponse sut = new ErrorResponse(
                    VALID_CODE,
                    CUSTOM_MESSAGE,
                    null,
                    createDetails(DETAILS_KEY, DETAILS_VALUE)
            );

            Instant upperBound = Instant.now();

            assertResponse(sut, VALID_CODE, CUSTOM_MESSAGE, lowerBound, upperBound, Map.of(DETAILS_KEY, DETAILS_VALUE));
        }

        @DisplayName("должен возвращать пустые детали, если переданы null или пустые")
        @ParameterizedTest(name = "значение details: {0}")
        @NullSource
        @MethodSource("io.github.sergeysenin.userservice.exception.dto.ErrorResponseTest#emptyDetails")
        void shouldReturnEmptyDetailsWhenDetailsNullOrEmpty(Map<String, String> details) {
            ErrorResponse sut = new ErrorResponse(VALID_CODE, CUSTOM_MESSAGE, Instant.now(), details);

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
                    () -> new ErrorResponse(
                            null,
                            CUSTOM_MESSAGE,
                            Instant.now(),
                            createDetails(DETAILS_KEY, DETAILS_VALUE)
                    ),
                    "Конструктор должен проверять код на null");

            assertAll("Проверка данных исключения",
                    () -> assertEquals("code не может быть null", exception.getMessage(),
                            "Сообщение исключения должно содержать причину отсутствия кода"));
        }

        @Test
        @DisplayName("должен выбрасывать NullPointerException, если сообщение равно null")
        void shouldThrowNullPointerExceptionWhenMessageIsNull() {
            NullPointerException exception = assertThrows(NullPointerException.class,
                    () -> new ErrorResponse(VALID_CODE, null, Instant.now(), createDetails(DETAILS_KEY, DETAILS_VALUE)),
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

            ErrorResponse sut = ErrorResponse.of(
                    TEST_ERROR_CODE,
                    CUSTOM_MESSAGE,
                    createDetails(DETAILS_KEY, DETAILS_VALUE)
            );

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

            ErrorResponse sut = ErrorResponse.of(
                    TEST_ERROR_CODE,
                    message,
                    createDetails(DETAILS_KEY, DETAILS_VALUE)
            );

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
            ErrorResponse sut = ErrorResponse.of(TEST_ERROR_CODE, CUSTOM_MESSAGE, null);

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
                    () -> ErrorResponse.of(null, CUSTOM_MESSAGE, createDetails(DETAILS_KEY, DETAILS_VALUE)),
                    "Фабричный метод должен проверять errorCode на null");

            assertAll("Проверка данных исключения",
                    () -> assertEquals("errorCode не может быть null", exception.getMessage(),
                            "Сообщение исключения должно содержать причину отсутствия errorCode"));
        }
    }
}
