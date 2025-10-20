package io.github.sergeysenin.userservice.exception.handler;

import io.github.sergeysenin.userservice.exception.code.ErrorCode;
import io.github.sergeysenin.userservice.exception.dto.ErrorResponse;
import io.github.sergeysenin.userservice.exception.type.base.BaseServiceException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    private static final String CONSTRAINT_EXCEPTION_MESSAGE = "validation failed";
    private static final String VALIDATION_ERROR_FALLBACK = "Validation error";
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
            Instant lowerTimestampBound = Instant.now();

            ResponseEntity<ErrorResponse> response = sut.handleMethodArgumentNotValid(exception);
            Instant upperTimestampBound = Instant.now();

            Map<String, String> expectedDetails = new LinkedHashMap<>();
            expectedDetails.put(FIELD_NAME, FIELD_ERROR_MESSAGE);
            expectedDetails.put(OBJECT_NAME, OBJECT_ERROR_CODE);

            assertResponse(
                    response,
                    ErrorCode.VALIDATION_FAILED,
                    ErrorCode.VALIDATION_FAILED.getDefaultMessage(),
                    lowerTimestampBound,
                    upperTimestampBound,
                    expectedDetails
            );
            assertDetailsImmutable(response.getBody());
        }

        @Test
        @DisplayName("должен вернуть ошибку валидации при ConstraintViolationException")
        void shouldReturnValidationFailedResponseWhenConstraintViolationOccurs() {
            ConstraintViolationStub messageViolation = createConstraintViolationStub(
                    CONSTRAINT_PROPERTY,
                    CONSTRAINT_MESSAGE,
                    null
            );
            ConstraintViolationStub templateViolation = createConstraintViolationStub(
                    TEMPLATE_ONLY_PROPERTY,
                    " ",
                    TEMPLATE_MESSAGE
            );
            Set<ConstraintViolation<?>> violations = orderedViolations(messageViolation, templateViolation);
            ConstraintViolationException exception = new OrderedConstraintViolationException(violations);
            GlobalExceptionHandler sut = createSut();
            Instant lowerTimestampBound = Instant.now();

            ResponseEntity<ErrorResponse> response = sut.handleConstraintViolation(exception);
            Instant upperTimestampBound = Instant.now();

            Map<String, String> expectedDetails = new LinkedHashMap<>();
            expectedDetails.put(CONSTRAINT_PROPERTY, CONSTRAINT_MESSAGE);
            expectedDetails.put(TEMPLATE_ONLY_PROPERTY, TEMPLATE_MESSAGE);

            assertResponse(
                    response,
                    ErrorCode.VALIDATION_FAILED,
                    ErrorCode.VALIDATION_FAILED.getDefaultMessage(),
                    lowerTimestampBound,
                    upperTimestampBound,
                    expectedDetails
            );
            assertDetailsImmutable(response.getBody());
            verifyConstraintViolationInteractions(messageViolation, false);
            verifyConstraintViolationInteractions(templateViolation, true);
        }

        @Test
        @DisplayName("должен вернуть ошибку биндинга при BindException")
        void shouldReturnBindingErrorResponseWhenBindExceptionOccurs() {
            BindException exception = createBindException();
            GlobalExceptionHandler sut = createSut();
            Instant lowerTimestampBound = Instant.now();

            ResponseEntity<ErrorResponse> response = sut.handleBindException(exception);
            Instant upperTimestampBound = Instant.now();

            Map<String, String> expectedDetails = new LinkedHashMap<>();
            expectedDetails.put(FIELD_NAME, FIELD_ERROR_MESSAGE);
            expectedDetails.put(OBJECT_NAME, OBJECT_ERROR_CODE);

            assertResponse(
                    response,
                    ErrorCode.BINDING_ERROR,
                    ErrorCode.BINDING_ERROR.getDefaultMessage(),
                    lowerTimestampBound,
                    upperTimestampBound,
                    expectedDetails
            );
            assertDetailsImmutable(response.getBody());
        }

        @Test
        @DisplayName("должен использовать сообщение по умолчанию когда отсутствуют текст и код ошибки")
        void shouldUseFallbackMessageWhenBindingErrorHasNoDetails() {
            BindingResult bindingResult = new BeanPropertyBindingResult(new SampleRequest(), OBJECT_NAME);
            bindingResult.addError(new ObjectError(OBJECT_NAME, new String[] {null}, null, null));
            BindException exception = new BindException(bindingResult);
            GlobalExceptionHandler sut = createSut();
            Instant lowerTimestampBound = Instant.now();

            ResponseEntity<ErrorResponse> response = sut.handleBindException(exception);
            Instant upperTimestampBound = Instant.now();

            Map<String, String> expectedDetails = Map.of(OBJECT_NAME, VALIDATION_ERROR_FALLBACK);

            assertResponse(
                    response,
                    ErrorCode.BINDING_ERROR,
                    ErrorCode.BINDING_ERROR.getDefaultMessage(),
                    lowerTimestampBound,
                    upperTimestampBound,
                    expectedDetails
            );
            assertDetailsImmutable(response.getBody());
        }

        @Test
        @DisplayName("должен сохранить первое сообщение при коллизии ключей ограничений")
        void shouldKeepFirstConstraintMessageWhenPropertyCollides() {
            ConstraintViolationStub firstViolation = createConstraintViolationStub(
                    CONSTRAINT_PROPERTY,
                    CONSTRAINT_MESSAGE,
                    null
            );
            ConstraintViolationStub secondViolation = createConstraintViolationStub(
                    CONSTRAINT_PROPERTY,
                    TEMPLATE_MESSAGE,
                    null
            );
            Set<ConstraintViolation<?>> violations = orderedViolations(firstViolation, secondViolation);
            ConstraintViolationException exception = new OrderedConstraintViolationException(violations);
            GlobalExceptionHandler sut = createSut();
            Instant lowerTimestampBound = Instant.now();

            ResponseEntity<ErrorResponse> response = sut.handleConstraintViolation(exception);
            Instant upperTimestampBound = Instant.now();

            Map<String, String> expectedDetails = Map.of(CONSTRAINT_PROPERTY, CONSTRAINT_MESSAGE);

            assertResponse(
                    response,
                    ErrorCode.VALIDATION_FAILED,
                    ErrorCode.VALIDATION_FAILED.getDefaultMessage(),
                    lowerTimestampBound,
                    upperTimestampBound,
                    expectedDetails
            );
            assertDetailsImmutable(response.getBody());
            verifyConstraintViolationInteractions(firstViolation, false);
            verifyConstraintViolationInteractions(secondViolation, false);
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
            Instant lowerTimestampBound = Instant.now();

            ResponseEntity<ErrorResponse> response = sut.handleDatabaseConstraintViolation(exception);
            Instant upperTimestampBound = Instant.now();

            Map<String, String> expectedDetails = new LinkedHashMap<>();
            expectedDetails.put("constraint", DATABASE_CONSTRAINT);

            assertResponse(
                    response,
                    ErrorCode.CONSTRAINT_VIOLATION,
                    ErrorCode.CONSTRAINT_VIOLATION.getDefaultMessage(),
                    lowerTimestampBound,
                    upperTimestampBound,
                    expectedDetails
            );
            assertDetailsImmutable(response.getBody());
        }

        @Test
        @DisplayName("должен опустить детали при отсутствии имени ограничения")
        void shouldOmitDetailsWhenDatabaseConstraintNameMissing() {
            org.hibernate.exception.ConstraintViolationException exception =
                    new org.hibernate.exception.ConstraintViolationException(
                            "Ошибка ограничения",
                            new SQLException("SQL state"),
                            "  "
                    );

            GlobalExceptionHandler sut = createSut();
            Instant lowerTimestampBound = Instant.now();

            ResponseEntity<ErrorResponse> response = sut.handleDatabaseConstraintViolation(exception);
            Instant upperTimestampBound = Instant.now();

            assertResponse(
                    response,
                    ErrorCode.CONSTRAINT_VIOLATION,
                    ErrorCode.CONSTRAINT_VIOLATION.getDefaultMessage(),
                    lowerTimestampBound,
                    upperTimestampBound,
                    Map.of()
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
            Instant lowerTimestampBound = Instant.now();

            ResponseEntity<ErrorResponse> response = sut.handleEntityNotFound(exception);
            Instant upperTimestampBound = Instant.now();

            assertResponse(
                    response,
                    ErrorCode.ENTITY_NOT_FOUND,
                    ENTITY_ERROR_MESSAGE,
                    lowerTimestampBound,
                    upperTimestampBound,
                    Map.of()
            );
        }

        @Test
        @DisplayName("должен вернуть 404 при NoSuchElementException")
        void shouldReturnNotFoundResponseWhenNoSuchElementExceptionOccurs() {
            NoSuchElementException exception = new NoSuchElementException(ENTITY_ERROR_MESSAGE);
            GlobalExceptionHandler sut = createSut();
            Instant lowerTimestampBound = Instant.now();

            ResponseEntity<ErrorResponse> response = sut.handleNoSuchElement(exception);
            Instant upperTimestampBound = Instant.now();

            assertResponse(
                    response,
                    ErrorCode.ENTITY_NOT_FOUND,
                    ENTITY_ERROR_MESSAGE,
                    lowerTimestampBound,
                    upperTimestampBound,
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
            Instant lowerTimestampBound = Instant.now();

            ResponseEntity<ErrorResponse> response = sut.handleBaseServiceException(exception);
            Instant upperTimestampBound = Instant.now();

            assertResponse(
                    response,
                    ErrorCode.USER_NOT_FOUND,
                    exception.getMessage(),
                    lowerTimestampBound,
                    upperTimestampBound,
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
            Instant lowerTimestampBound = Instant.now();

            ResponseEntity<ErrorResponse> response = sut.handleRuntimeException(exception);
            Instant upperTimestampBound = Instant.now();

            assertResponse(
                    response,
                    ErrorCode.UNEXPECTED_ERROR,
                    ErrorCode.UNEXPECTED_ERROR.getDefaultMessage(),
                    lowerTimestampBound,
                    upperTimestampBound,
                    Map.of()
            );
        }

        @Test
        @DisplayName("должен вернуть 500 при Exception")
        void shouldReturnUnexpectedErrorWhenExceptionOccurs() {
            Exception exception = new Exception("Неизвестная ошибка");
            GlobalExceptionHandler sut = createSut();
            Instant lowerTimestampBound = Instant.now();

            ResponseEntity<ErrorResponse> response = sut.handleException(exception);
            Instant upperTimestampBound = Instant.now();

            assertResponse(
                    response,
                    ErrorCode.UNEXPECTED_ERROR,
                    ErrorCode.UNEXPECTED_ERROR.getDefaultMessage(),
                    lowerTimestampBound,
                    upperTimestampBound,
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
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new SampleRequest(), OBJECT_NAME);
        bindingResult.addError(new FieldError(OBJECT_NAME, FIELD_NAME, FIELD_ERROR_MESSAGE));
        bindingResult.addError(new ObjectError(OBJECT_NAME, new String[] {OBJECT_ERROR_CODE}, null, " "));
        return bindingResult;
    }

    private void assertResponse(
            ResponseEntity<ErrorResponse> response,
            ErrorCode expectedErrorCode,
            String expectedMessage,
            Instant lowerTimestampBound,
            Instant upperTimestampBound,
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
                () -> assertAll("Проверка временной метки ответа",
                        () -> assertNotNull(body.timestamp(), "Временная метка не должна быть null"),
                        () -> assertTrue(
                                !body.timestamp().isBefore(lowerTimestampBound),
                                "Временная метка должна быть не раньше нижней границы"
                        ),
                        () -> assertTrue(
                                !body.timestamp().isAfter(upperTimestampBound),
                                "Временная метка должна быть не позже верхней границы"
                        )),
                () -> assertEquals(
                        expectedDetails,
                        body.details(),
                        "Детали ошибки должны совпадать"
                ));
    }

    private void assertDetailsImmutable(ErrorResponse response) {
        assertNotNull(response, "Ответ не должен быть null");
        UnsupportedOperationException exception = assertThrows(
                UnsupportedOperationException.class,
                () -> response.details().put("new", "value"),
                "Коллекция деталей должна быть неизменяемой"
        );
        assertAll("Проверка исключения неизменяемости деталей",
                () -> assertEquals(
                        UnsupportedOperationException.class,
                        exception.getClass(),
                        "Тип исключения должен соответствовать UnsupportedOperationException"
                ),
                () -> assertNull(
                        exception.getMessage(),
                        "Сообщение исключения должно отсутствовать"
                ));
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

    private ConstraintViolationStub createConstraintViolationStub(String property, String message, String template) {
        return new ConstraintViolationStub(property, message, template);
    }

    private void verifyConstraintViolationInteractions(ConstraintViolationStub stub, boolean expectsTemplate) {
        assertAll("Проверка обращений к нарушению ограничения",
                () -> assertTrue(
                        stub.propertyPathCalls() > 0,
                        "Должен быть вызван доступ к пути свойства"
                ),
                () -> assertTrue(
                        stub.messageCalls() > 0,
                        "Должно быть считано сообщение об ограничении"
                ),
                () -> assertEquals(
                        expectsTemplate ? 1 : 0,
                        stub.templateCalls(),
                        "Количество обращений к шаблону сообщения должно соответствовать ожиданиям"
                ));
    }

    private Set<ConstraintViolation<?>> orderedViolations(ConstraintViolationStub... violations) {
        return new LinkedHashSet<>(java.util.List.of(violations));
    }

    private static class OrderedConstraintViolationException extends ConstraintViolationException {

        private final Set<ConstraintViolation<?>> orderedViolations;

        OrderedConstraintViolationException(Set<ConstraintViolation<?>> orderedViolations) {
            super(CONSTRAINT_EXCEPTION_MESSAGE, orderedViolations);
            this.orderedViolations = new LinkedHashSet<>(orderedViolations);
        }

        @Override
        public Set<ConstraintViolation<?>> getConstraintViolations() {
            return orderedViolations;
        }
    }

    private static final class ConstraintViolationStub implements ConstraintViolation<Object> {

        private final Path propertyPath;
        private final String message;
        private final String template;
        private int propertyPathCalls;
        private int messageCalls;
        private int templateCalls;

        private ConstraintViolationStub(String property, String message, String template) {
            this.propertyPath = new SimplePath(property);
            this.message = message;
            this.template = template;
        }

        @Override
        public String getMessage() {
            messageCalls++;
            return message;
        }

        @Override
        public String getMessageTemplate() {
            templateCalls++;
            return template;
        }

        @Override
        public Path getPropertyPath() {
            propertyPathCalls++;
            return propertyPath;
        }

        @Override
        public Object getRootBean() {
            return null;
        }

        @Override
        public Class<Object> getRootBeanClass() {
            return Object.class;
        }

        @Override
        public Object getLeafBean() {
            return null;
        }

        @Override
        public Object[] getExecutableParameters() {
            return new Object[0];
        }

        @Override
        public Object getExecutableReturnValue() {
            return null;
        }

        @Override
        public Object getInvalidValue() {
            return null;
        }

        @Override
        public ConstraintDescriptor<?> getConstraintDescriptor() {
            return null;
        }

        @Override
        public <U> U unwrap(Class<U> type) {
            throw new UnsupportedOperationException("unwrap не поддерживается в тестовом стабе");
        }

        private int propertyPathCalls() {
            return propertyPathCalls;
        }

        private int messageCalls() {
            return messageCalls;
        }

        private int templateCalls() {
            return templateCalls;
        }
    }

    private record SimplePath(String representation) implements Path {
        @Override
        @NonNull
        public Iterator<Node> iterator() {
            return java.util.Collections.emptyIterator();
        }

        @Override
        public String toString() {
            return representation;
        }
    }
}
