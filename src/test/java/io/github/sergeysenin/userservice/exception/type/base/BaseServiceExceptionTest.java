package io.github.sergeysenin.userservice.exception.type.base;

import io.github.sergeysenin.userservice.exception.code.ErrorCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("BaseServiceException")
class BaseServiceExceptionTest {

    private static final ErrorCode TEST_ERROR_CODE = ErrorCode.USER_NOT_FOUND;
    private static final String CUSTOM_MESSAGE = "Пользователь не найден в системе";
    private static final String BLANK_MESSAGE = "   ";
    private static final String DETAILS_KEY = "username";
    private static final String DETAILS_VALUE = "john.doe";
    private static final String NULL_KEY_EXCEPTION_MESSAGE = "details содержит null-ключ";
    private static final String NULL_VALUE_EXCEPTION_MESSAGE =
            "details содержит null-значение для ключа '" + DETAILS_KEY + "'";
    private static final String CAUSE_MESSAGE = "test cause";
    private static final Map<String, String> EMPTY_DETAILS = Map.of();
    private static final int EXPECTED_SINGLE_DETAIL_COUNT = 1;

    @Nested
    @DisplayName("Конструкторы: обработка сообщения")
    class ConstructorMessageTests {

        @DisplayName("должен использовать сообщение по умолчанию, когда message null или пустое")
        @ParameterizedTest(name = "{displayName} [{index}]")
        @MethodSource("io.github.sergeysenin.userservice.exception.type.base.BaseServiceExceptionTest#invalidMessages")
        void shouldUseDefaultMessageWhenMessageNullOrBlank(String message) {
            var sut = createSut(TEST_ERROR_CODE, message);

            assertAll("Сообщение должно заменяться на дефолтное",
                    () -> assertEquals(
                            TEST_ERROR_CODE.getDefaultMessage(),
                            sut.getMessage(),
                            "Сообщение должно совпадать с дефолтным значением"
                    ),
                    () -> assertSame(TEST_ERROR_CODE, sut.getErrorCode(), "Должен сохраняться errorCode"));
        }

        @Test
        @DisplayName("должен сохранять пользовательское сообщение, когда оно передано")
        void shouldStoreProvidedMessageWhenMessageNotBlank() {
            var sut = createSut(TEST_ERROR_CODE, CUSTOM_MESSAGE);

            assertAll("Сообщение должно совпадать с пользовательским",
                    () -> assertEquals(
                            CUSTOM_MESSAGE,
                            sut.getMessage(),
                            "Сообщение должно совпадать с переданным значением"
                    ),
                    () -> assertSame(TEST_ERROR_CODE, sut.getErrorCode(), "Должен сохраняться errorCode"));
        }

        @Test
        @DisplayName("должен сохранять причину, когда она передана")
        void shouldPropagateCauseWhenProvided() {
            var cause = new IllegalStateException(CAUSE_MESSAGE);

            var sut = createSut(TEST_ERROR_CODE, CUSTOM_MESSAGE, cause);

            assertAll("Причина должна сохраняться",
                    () -> assertEquals(
                            CUSTOM_MESSAGE,
                            sut.getMessage(),
                            "Сообщение должно совпадать с переданным значением"
                    ),
                    () -> assertSame(cause, sut.getCause(), "Причина должна совпадать с переданной"));
        }
    }

    @Nested
    @DisplayName("Конструкторы: обработка деталей")
    class ConstructorDetailsTests {

        @DisplayName("должен возвращать пустые детали, когда details null или пуст")
        @ParameterizedTest(name = "{displayName} [{index}]")
        @MethodSource("io.github.sergeysenin.userservice.exception.type.base.BaseServiceExceptionTest#emptyDetails")
        void shouldReturnEmptyDetailsWhenDetailsNullOrEmpty(Map<String, String> sourceDetails) {
            var sut = createSutWithDetails(TEST_ERROR_CODE, CUSTOM_MESSAGE, sourceDetails);

            assertAll("Проверка пустых деталей при отсутствии данных",
                    () -> assertNotNull(sut.getDetails(), "Коллекция деталей не должна быть null"),
                    () -> assertTrue(
                            sut.getDetails().isEmpty(),
                            "Детали должны быть пустыми при передаче null или пустой коллекции"
                    ));
        }

        @Test
        @DisplayName("должен создавать неизменяемую копию деталей")
        void shouldCreateImmutableCopyWhenDetailsProvided() {
            var originalDetails = createDetails();

            var sut = createSutWithDetails(TEST_ERROR_CODE, CUSTOM_MESSAGE, originalDetails);

            originalDetails.put("another", "value");

            assertAll("Проверка копирования и неизменности деталей",
                    () -> assertEquals(
                            EXPECTED_SINGLE_DETAIL_COUNT,
                            sut.getDetails().size(),
                            "Размер деталей не должен измениться после модификации исходной карты"
                    ),
                    () -> assertEquals(
                            DETAILS_VALUE,
                            sut.getDetails().get(DETAILS_KEY),
                            "Значение должно соответствовать исходному"
                    ),
                    () -> assertThrows(
                            UnsupportedOperationException.class,
                            () -> sut.getDetails().put("new", "value"),
                            "Коллекция деталей должна быть неизменяемой"
                    ));
        }

        @Test
        @DisplayName("должен выбрасывать NullPointerException, когда details содержит null-ключ")
        void shouldThrowNullPointerExceptionWhenDetailsContainsNullKey() {
            var detailsWithNullKey = new HashMap<String, String>();
            detailsWithNullKey.put(null, DETAILS_VALUE);

            var exception = assertThrows(
                    NullPointerException.class,
                    () -> createSutWithDetails(TEST_ERROR_CODE, CUSTOM_MESSAGE, detailsWithNullKey),
                    "Ожидается NullPointerException при наличии null-ключа"
            );

            assertAll("Проверка данных исключения",
                    () -> assertNotNull(exception.getMessage(), "Сообщение исключения не должно быть null"),
                    () -> assertEquals(
                            NULL_KEY_EXCEPTION_MESSAGE,
                            exception.getMessage(),
                            "Сообщение исключения должно информировать о null-ключе"
                    ));
        }

        @Test
        @DisplayName("должен выбрасывать NullPointerException, когда details содержит null-значение")
        void shouldThrowNullPointerExceptionWhenDetailsContainsNullValue() {
            var detailsWithNullValue = new HashMap<String, String>();
            detailsWithNullValue.put(DETAILS_KEY, null);

            var exception = assertThrows(
                    NullPointerException.class,
                    () -> createSutWithDetails(TEST_ERROR_CODE, CUSTOM_MESSAGE, detailsWithNullValue),
                    "Ожидается NullPointerException при наличии null-значения"
            );

            assertAll("Проверка данных исключения",
                    () -> assertNotNull(exception.getMessage(), "Сообщение исключения не должно быть null"),
                    () -> assertEquals(
                            NULL_VALUE_EXCEPTION_MESSAGE,
                            exception.getMessage(),
                            "Сообщение исключения должно информировать о null-значении"
                    ));
        }
    }

    @Nested
    @DisplayName("Валидация обязательных аргументов")
    class RequiredArgumentsTests {

        @Test
        @DisplayName("должен выбрасывать NullPointerException, когда errorCode равен null")
        void shouldThrowNullPointerExceptionWhenErrorCodeIsNull() {
            var exception = assertThrows(
                    NullPointerException.class,
                    () -> createSut(null, CUSTOM_MESSAGE),
                    "Ожидается NullPointerException при отсутствии errorCode"
            );

            assertAll("Проверка данных исключения",
                    () -> assertNotNull(exception.getMessage(), "Сообщение исключения не должно быть null"),
                    () -> assertEquals(
                            "errorCode не может быть null",
                            exception.getMessage(),
                            "Сообщение исключения должно информировать о null errorCode"
                    ));
        }
    }

    @Nested
    @DisplayName("Преобразование в ErrorResponse")
    class ErrorResponseConversionTests {

        @Test
        @DisplayName("должен создавать ErrorResponse с актуальными данными")
        void shouldBuildErrorResponseWithCurrentState() {
            var details = createDetails();
            var sut = createSutWithDetails(TEST_ERROR_CODE, CUSTOM_MESSAGE, details);

            var lowerTimestampBound = Instant.now();

            var response = sut.toErrorResponse();

            var upperTimestampBound = Instant.now();

            assertAll("Проверка содержимого ErrorResponse",
                    () -> assertEquals(
                            TEST_ERROR_CODE.getCode(),
                            response.code(),
                            "Код ошибки должен совпадать"
                    ),
                    () -> assertEquals(
                            CUSTOM_MESSAGE,
                            response.message(),
                            "Сообщение должно совпадать с текущим состоянием исключения"
                    ),
                    () -> assertNotNull(response.timestamp(), "Timestamp должен быть заполнен"),
                    () -> assertAll("Проверка диапазона timestamp",
                            () -> assertTrue(
                                    !response.timestamp().isBefore(lowerTimestampBound),
                                    "Timestamp не должен быть раньше нижней границы"
                            ),
                            () -> assertTrue(
                                    !response.timestamp().isAfter(upperTimestampBound),
                                    "Timestamp не должен быть позже верхней границы"
                            )
                    ),
                    () -> assertEquals(
                            details,
                            response.details(),
                            "Детали должны копироваться из исключения"
                    ),
                    () -> assertThrows(
                            UnsupportedOperationException.class,
                            () -> response.details().put("new", "value"),
                            "Детали ответа должны быть неизменяемыми"
                    ));
        }
    }

    private Map<String, String> createDetails() {
        var details = new HashMap<String, String>();
        details.put(DETAILS_KEY, DETAILS_VALUE);
        return details;
    }

    private BaseServiceException createSut(ErrorCode errorCode, String message) {
        return new TestServiceException(errorCode, message);
    }

    private BaseServiceException createSut(ErrorCode errorCode, String message, Throwable cause) {
        return new TestServiceException(errorCode, message, null, cause);
    }

    private BaseServiceException createSutWithDetails(
            ErrorCode errorCode,
            String message,
            Map<String, String> details
    ) {
        return new TestServiceException(errorCode, message, details, null);
    }

    private static Stream<String> invalidMessages() {
        return Stream.of(null, BLANK_MESSAGE);
    }

    private static Stream<Map<String, String>> emptyDetails() {
        return Stream.of(null, EMPTY_DETAILS);
    }

    private static final class TestServiceException extends BaseServiceException {

        private TestServiceException(ErrorCode errorCode, String message) {
            super(errorCode, message);
        }

        private TestServiceException(
                ErrorCode errorCode,
                String message,
                Map<String, String> details,
                Throwable cause
        ) {
            super(errorCode, message, details, cause);
        }
    }
}
