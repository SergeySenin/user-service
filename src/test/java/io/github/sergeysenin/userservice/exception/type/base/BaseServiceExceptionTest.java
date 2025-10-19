package io.github.sergeysenin.userservice.exception.type.base;

import io.github.sergeysenin.userservice.exception.code.ErrorCode;
import io.github.sergeysenin.userservice.exception.dto.ErrorResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

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
    private static final String DETAILS_KEY = "username";
    private static final String DETAILS_VALUE = "john.doe";
    private static final String NULL_KEY_EXCEPTION_MESSAGE = "details содержит null-ключ";
    private static final String NULL_VALUE_EXCEPTION_MESSAGE_PREFIX = "details содержит null-значение";

    @Nested
    @DisplayName("Конструкторы: обработка сообщения")
    class ConstructorMessageTests {

        @Test
        @DisplayName("должен использовать сообщение по умолчанию, когда message равно null")
        void shouldUseDefaultMessageWhenMessageNull() {
            BaseServiceException sut = createSut(TEST_ERROR_CODE, null);

            assertAll("Сообщение должно заменяться на дефолтное",
                    () -> assertEquals(
                            TEST_ERROR_CODE.getDefaultMessage(),
                            sut.getMessage(),
                            "Сообщение должно совпадать с дефолтным значением"
                    ),
                    () -> assertSame(TEST_ERROR_CODE, sut.getErrorCode(), "Должен сохраняться errorCode"));
        }

        @Test
        @DisplayName("должен использовать сообщение по умолчанию, когда message содержит только пробелы")
        void shouldUseDefaultMessageWhenMessageBlank() {
            BaseServiceException sut = createSut(TEST_ERROR_CODE, "   ");

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
            BaseServiceException sut = createSut(TEST_ERROR_CODE, CUSTOM_MESSAGE);

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
            Throwable cause = new IllegalStateException("test cause");

            BaseServiceException sut = createSut(TEST_ERROR_CODE, CUSTOM_MESSAGE, cause);

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

        @Test
        @DisplayName("должен возвращать пустые детали, когда details равно null")
        void shouldReturnEmptyDetailsWhenDetailsNull() {
            BaseServiceException sut = createSutWithDetails(TEST_ERROR_CODE, CUSTOM_MESSAGE, null);

            assertAll("Детали должны быть пустыми",
                    () -> assertNotNull(sut.getDetails(), "Коллекция деталей не должна быть null"),
                    () -> assertTrue(sut.getDetails().isEmpty(), "Детали должны быть пустыми"));
        }

        @Test
        @DisplayName("должен возвращать пустые детали, когда details пуст")
        void shouldReturnEmptyDetailsWhenDetailsEmpty() {
            BaseServiceException sut = createSutWithDetails(TEST_ERROR_CODE, CUSTOM_MESSAGE, Map.of());

            assertAll("Детали должны быть пустыми",
                    () -> assertNotNull(sut.getDetails(), "Коллекция деталей не должна быть null"),
                    () -> assertTrue(sut.getDetails().isEmpty(), "Детали должны быть пустыми"));
        }

        @Test
        @DisplayName("должен создавать неизменяемую копию деталей")
        void shouldCreateImmutableCopyWhenDetailsProvided() {
            Map<String, String> originalDetails = createDetails();

            BaseServiceException sut = createSutWithDetails(TEST_ERROR_CODE, CUSTOM_MESSAGE, originalDetails);

            originalDetails.put("another", "value");

            assertAll("Детали должны копироваться и быть неизменяемыми",
                    () -> assertEquals(
                            1,
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
            Map<String, String> detailsWithNullKey = new HashMap<>();
            detailsWithNullKey.put(null, DETAILS_VALUE);

            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> createSutWithDetails(TEST_ERROR_CODE, CUSTOM_MESSAGE, detailsWithNullKey),
                    "Ожидается NullPointerException при наличии null-ключа"
            );

            assertEquals(
                    NULL_KEY_EXCEPTION_MESSAGE,
                    exception.getMessage(),
                    "Сообщение исключения должно информировать о null-ключе"
            );
        }

        @Test
        @DisplayName("должен выбрасывать NullPointerException, когда details содержит null-значение")
        void shouldThrowNullPointerExceptionWhenDetailsContainsNullValue() {
            Map<String, String> detailsWithNullValue = new HashMap<>();
            detailsWithNullValue.put(DETAILS_KEY, null);

            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> createSutWithDetails(TEST_ERROR_CODE, CUSTOM_MESSAGE, detailsWithNullValue),
                    "Ожидается NullPointerException при наличии null-значения"
            );

            assertTrue(
                    exception.getMessage() != null &&
                            exception.getMessage().startsWith(NULL_VALUE_EXCEPTION_MESSAGE_PREFIX),
                    "Сообщение исключения должно информировать о null-значении"
            );
        }
    }

    @Nested
    @DisplayName("Валидация обязательных аргументов")
    class RequiredArgumentsTests {

        @Test
        @DisplayName("должен выбрасывать NullPointerException, когда errorCode равен null")
        void shouldThrowNullPointerExceptionWhenErrorCodeIsNull() {
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> createSut(null, CUSTOM_MESSAGE),
                    "Ожидается NullPointerException при отсутствии errorCode"
            );

            assertEquals(
                    "errorCode не может быть null",
                    exception.getMessage(),
                    "Сообщение исключения должно информировать о null errorCode"
            );
        }
    }

    @Nested
    @DisplayName("Преобразование в ErrorResponse")
    class ErrorResponseConversionTests {

        @Test
        @DisplayName("должен создавать ErrorResponse с актуальными данными")
        void shouldBuildErrorResponseWithCurrentState() {
            Map<String, String> details = createDetails();
            BaseServiceException sut = createSutWithDetails(
                    TEST_ERROR_CODE,
                    CUSTOM_MESSAGE,
                    details
            );

            ErrorResponse response = sut.toErrorResponse();

            assertAll("ErrorResponse должен содержать корректные данные",
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
        Map<String, String> details = new HashMap<>();
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
