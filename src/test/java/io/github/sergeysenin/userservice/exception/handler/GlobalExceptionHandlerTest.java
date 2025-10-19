package io.github.sergeysenin.userservice.exception.handler;

import io.github.sergeysenin.userservice.exception.code.ErrorCode;
import io.github.sergeysenin.userservice.exception.dto.ErrorResponse;
import io.github.sergeysenin.userservice.exception.type.base.BaseServiceException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private static final String OBJECT_NAME = "sampleRequest";
    private static final String FIELD_NAME = "email";
    private static final String FIELD_ERROR_MESSAGE = "Поле email обязательно";
    private static final String OBJECT_ERROR_CODE = "ObjectErrorCode";
    private static final String CONSTRAINT_PROPERTY = "user.name";
    private static final String CONSTRAINT_MESSAGE = "Имя обязательно";
    private static final String TEMPLATE_ONLY_PROPERTY = "user.password";
    private static final String TEMPLATE_MESSAGE = "{Password.NotBlank}";
    private static final String DATABASE_CONSTRAINT = "uq_users_username";
    private static final String ENTITY_ERROR_MESSAGE = "Пользователь не найден";
    private static final String BUSINESS_DETAIL_KEY = "userId";
    private static final String BUSINESS_DETAIL_VALUE = "42";

    private GlobalExceptionHandler createSut() {
        return new GlobalExceptionHandler();
    }

    @Nested
    @DisplayName("Валидационные исключения")
    class ValidationExceptionTests {

        @Test
        @DisplayName("должен вернуть ошибку валидации при MethodArgumentNotValidException")
        void shouldReturnValidationFailedResponseWhenMethodArgumentInvalid() throws NoSuchMethodException {
            MethodArgumentNotValidException exception = createMethodArgumentNotValidException();
            GlobalExceptionHandler sut = createSut();

            ResponseEntity<ErrorResponse> response = sut.handleMethodArgumentNotValid(exception);

            Map<String, String> expectedDetails = new LinkedHashMap<>();
            expectedDetails.put(FIELD_NAME, FIELD_ERROR_MESSAGE);
            expectedDetails.put(OBJECT_NAME, OBJECT_ERROR_CODE);

            assertResponse(
                    response,
                    ErrorCode.VALIDATION_FAILED,
                    ErrorCode.VALIDATION_FAILED.getDefaultMessage(),
                    expectedDetails
            );
            assertDetailsImmutable(response.getBody());
        }

        @Test
        @DisplayName("должен вернуть ошибку валидации при ConstraintViolationException")
        void shouldReturnValidationFailedResponseWhenConstraintViolationOccurs() {
            ConstraintViolationException exception = createConstraintViolationException();
            GlobalExceptionHandler sut = createSut();

            ResponseEntity<ErrorResponse> response = sut.handleConstraintViolation(exception);

            Map<String, String> expectedDetails = new LinkedHashMap<>();
            expectedDetails.put(CONSTRAINT_PROPERTY, CONSTRAINT_MESSAGE);
            expectedDetails.put(TEMPLATE_ONLY_PROPERTY, TEMPLATE_MESSAGE);

            assertResponse(
                    response,
                    ErrorCode.VALIDATION_FAILED,
                    ErrorCode.VALIDATION_FAILED.getDefaultMessage(),
                    expectedDetails
            );
            assertDetailsImmutable(response.getBody());
        }

        @Test
        @DisplayName("должен вернуть ошибку биндинга при BindException")
        void shouldReturnBindingErrorResponseWhenBindExceptionOccurs() {
            BindException exception = createBindException();
            GlobalExceptionHandler sut = createSut();

            ResponseEntity<ErrorResponse> response = sut.handleBindException(exception);

            Map<String, String> expectedDetails = new LinkedHashMap<>();
            expectedDetails.put(FIELD_NAME, FIELD_ERROR_MESSAGE);
            expectedDetails.put(OBJECT_NAME, OBJECT_ERROR_CODE);

            assertResponse(
                    response,
                    ErrorCode.BINDING_ERROR,
                    ErrorCode.BINDING_ERROR.getDefaultMessage(),
                    expectedDetails
            );
            assertDetailsImmutable(response.getBody());
        }
    }

    @Nested
    @DisplayName("Нарушения ограничений базы данных")
    class DatabaseConstraintTests {

        @Test
        @DisplayName("должен вернуть конфликт при нарушении уникальности БД")
        void shouldReturnConflictResponseWhenDatabaseConstraintViolated() {
            org.hibernate.exception.ConstraintViolationException exception =
                    new org.hibernate.exception.ConstraintViolationException(
                            "Ошибка ограничения",
                            new SQLException("SQL state"),
                            DATABASE_CONSTRAINT
                    );

            GlobalExceptionHandler sut = createSut();

            ResponseEntity<ErrorResponse> response = sut.handleDatabaseConstraintViolation(exception);

            Map<String, String> expectedDetails = new LinkedHashMap<>();
            expectedDetails.put("constraint", DATABASE_CONSTRAINT);

            assertResponse(
                    response,
                    ErrorCode.CONSTRAINT_VIOLATION,
                    ErrorCode.CONSTRAINT_VIOLATION.getDefaultMessage(),
                    expectedDetails
            );
            assertDetailsImmutable(response.getBody());
        }
    }

    @Nested
    @DisplayName("Поиск сущностей")
    class EntityLookupTests {

        @Test
        @DisplayName("должен вернуть 404 при EntityNotFoundException")
        void shouldReturnNotFoundResponseWhenEntityNotFoundExceptionOccurs() {
            EntityNotFoundException exception = new EntityNotFoundException(ENTITY_ERROR_MESSAGE);
            GlobalExceptionHandler sut = createSut();

            ResponseEntity<ErrorResponse> response = sut.handleEntityNotFound(exception);

            assertResponse(
                    response,
                    ErrorCode.ENTITY_NOT_FOUND,
                    ENTITY_ERROR_MESSAGE,
                    Map.of()
            );
        }

        @Test
        @DisplayName("должен вернуть 404 при NoSuchElementException")
        void shouldReturnNotFoundResponseWhenNoSuchElementExceptionOccurs() {
            NoSuchElementException exception = new NoSuchElementException(ENTITY_ERROR_MESSAGE);
            GlobalExceptionHandler sut = createSut();

            ResponseEntity<ErrorResponse> response = sut.handleNoSuchElement(exception);

            assertResponse(
                    response,
                    ErrorCode.ENTITY_NOT_FOUND,
                    ENTITY_ERROR_MESSAGE,
                    Map.of()
            );
        }
    }

    @Nested
    @DisplayName("Бизнес-исключения")
    class BusinessExceptionTests {

        @Test
        @DisplayName("должен делегировать создание ErrorResponse в BaseServiceException")
        void shouldUseBaseServiceExceptionPayloadWhenBusinessErrorOccurs() {
            Map<String, String> details = new LinkedHashMap<>();
            details.put(BUSINESS_DETAIL_KEY, BUSINESS_DETAIL_VALUE);
            TestServiceException exception = new TestServiceException(details);
            GlobalExceptionHandler sut = createSut();

            ResponseEntity<ErrorResponse> response = sut.handleBaseServiceException(exception);

            assertResponse(
                    response,
                    ErrorCode.USER_NOT_FOUND,
                    exception.getMessage(),
                    details
            );
            assertDetailsMatch(response.getBody(), exception.toErrorResponse());
        }
    }

    @Nested
    @DisplayName("Необработанные ошибки")
    class UnexpectedExceptionTests {

        @Test
        @DisplayName("должен вернуть 500 при RuntimeException")
        void shouldReturnUnexpectedErrorWhenRuntimeExceptionOccurs() {
            RuntimeException exception = new RuntimeException("Ошибка выполнения");
            GlobalExceptionHandler sut = createSut();

            ResponseEntity<ErrorResponse> response = sut.handleRuntimeException(exception);

            assertResponse(
                    response,
                    ErrorCode.UNEXPECTED_ERROR,
                    ErrorCode.UNEXPECTED_ERROR.getDefaultMessage(),
                    Map.of()
            );
        }

        @Test
        @DisplayName("должен вернуть 500 при Exception")
        void shouldReturnUnexpectedErrorWhenExceptionOccurs() {
            Exception exception = new Exception("Неизвестная ошибка");
            GlobalExceptionHandler sut = createSut();

            ResponseEntity<ErrorResponse> response = sut.handleException(exception);

            assertResponse(
                    response,
                    ErrorCode.UNEXPECTED_ERROR,
                    ErrorCode.UNEXPECTED_ERROR.getDefaultMessage(),
                    Map.of()
            );
        }
    }

    private MethodArgumentNotValidException createMethodArgumentNotValidException() throws NoSuchMethodException {
        BindingResult bindingResult = createBindingResult();
        Method method = SampleController.class.getDeclaredMethod("create", SampleRequest.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);
        return new MethodArgumentNotValidException(methodParameter, bindingResult);
    }

    private BindException createBindException() {
        BindingResult bindingResult = createBindingResult();
        return new BindException(bindingResult);
    }

    private BindingResult createBindingResult() {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new SampleRequest(), OBJECT_NAME);
        bindingResult.addError(
                new FieldError(OBJECT_NAME, FIELD_NAME, FIELD_ERROR_MESSAGE)
        );
        bindingResult.addError(
                new ObjectError(
                        OBJECT_NAME,
                        new String[] {OBJECT_ERROR_CODE},
                        null,
                        " "
                )
        );
        return bindingResult;
    }

    private ConstraintViolationException createConstraintViolationException() {
        ConstraintViolation<?> messageViolation = createConstraintViolation(
                CONSTRAINT_PROPERTY,
                CONSTRAINT_MESSAGE,
                null
        );
        ConstraintViolation<?> templateViolation = createConstraintViolation(
                TEMPLATE_ONLY_PROPERTY,
                " ",
                TEMPLATE_MESSAGE
        );
        Set<ConstraintViolation<?>> violations = new LinkedHashSet<>();
        violations.add(messageViolation);
        violations.add(templateViolation);
        return new ConstraintViolationException("validation failed", violations);
    }

    private ConstraintViolation<?> createConstraintViolation(
            String property,
            String message,
            String template
    ) {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn(property);
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn(message);
        if (template != null) {
            when(violation.getMessageTemplate()).thenReturn(template);
        }
        return violation;
    }

    private void assertResponse(
            ResponseEntity<ErrorResponse> response,
            ErrorCode expectedErrorCode,
            String expectedMessage,
            Map<String, String> expectedDetails
    ) {
        assertNotNull(response, "Ответ не должен быть null");
        ErrorResponse body = response.getBody();
        assertNotNull(body, "Тело ответа не должно быть null");
        assertAll("Проверка сформированного ответа",
                () -> assertEquals(
                        expectedErrorCode.getHttpStatus(),
                        response.getStatusCode(),
                        "HTTP статус должен соответствовать ожидаемому"
                ),
                () -> assertEquals(
                        expectedErrorCode.getCode(),
                        body.code(),
                        "Код ошибки должен совпадать"
                ),
                () -> assertEquals(
                        expectedMessage,
                        body.message(),
                        "Сообщение ошибки должно совпадать"
                ),
                () -> assertEquals(
                        expectedDetails,
                        body.details(),
                        "Детали ошибки должны совпадать"
                ));
    }

    private void assertDetailsImmutable(ErrorResponse response) {
        assertNotNull(response, "Ответ не должен быть null");
        assertThrows(
                UnsupportedOperationException.class,
                () -> response.details().put("new", "value"),
                "Коллекция деталей должна быть неизменяемой"
        );
    }

    private void assertDetailsMatch(ErrorResponse actual, ErrorResponse expected) {
        assertNotNull(actual, "Ответ не должен быть null");
        assertNotNull(expected, "Ожидаемый ответ не должен быть null");
        assertEquals(
                expected.details(),
                actual.details(),
                "Детали в ответе должны совпадать с деталями исключения"
        );
    }

    private static class SampleController {

        @SuppressWarnings("unused")
        void create(SampleRequest request) {
            // метод-заглушка для построения MethodParameter
        }
    }

    private static class SampleRequest {
        private String email;
    }

    private static class TestServiceException extends BaseServiceException {

        TestServiceException(Map<String, String> details) {
            super(ErrorCode.USER_NOT_FOUND, "", details);
        }
    }
}
