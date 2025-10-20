package io.github.sergeysenin.userservice.service.s3;

import io.github.sergeysenin.userservice.config.s3.S3Properties;
import io.github.sergeysenin.userservice.exception.type.FileStorageException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;

import static io.github.sergeysenin.userservice.testutil.net.UrlTestUtils.createUrl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DisplayName("S3Service")
class S3ServiceTest {

    private static final String ENDPOINT = "http://localhost";
    private static final String ACCESS_KEY = "accessKey";
    private static final String SECRET_KEY = "secretKey";
    private static final String BUCKET_NAME = "test-bucket";
    private static final String REGION = "us-east-1";
    private static final Duration URL_EXPIRATION = Duration.ofMinutes(10);
    private static final String VALID_KEY = "avatars/user-1/original.png";
    private static final String PRESIGNED_URL = "https://example.com/resource";
    private static final URL PRESIGNED_URL_OBJECT = createUrl(PRESIGNED_URL);
    private static final byte[] VALID_CONTENT = {1, 2, 3, 4};
    private static final byte[] EMPTY_CONTENT = {};
    private static final Instant PRESIGNED_EXPIRATION = Instant.parse("2024-01-01T00:00:00Z");
    private static final String EMPTY_KEY_MESSAGE = "Пустой ключ объекта";
    private static final String EMPTY_CONTENT_MESSAGE = "Пустое содержимое файла";
    private static final String SAVE_ERROR_PREFIX = "Не удалось сохранить файл в хранилище: ";
    private static final String DELETE_ERROR_PREFIX = "Не удалось удалить файл из хранилища: ";
    private static final String PRESIGN_ERROR_PREFIX = "Не удалось сгенерировать ссылку для файла: ";
    private static final S3Properties S3_PROPERTIES = new S3Properties(
            ENDPOINT,
            ACCESS_KEY,
            SECRET_KEY,
            BUCKET_NAME,
            REGION,
            URL_EXPIRATION
    );

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    private S3Service createSut() {
        return new S3Service(s3Client, s3Presigner, S3_PROPERTIES);
    }

    @Nested
    @DisplayName("storeObject")
    class StoreObject {

        @Test
        @DisplayName("должен бросать исключение, когда ключ пустой")
        void shouldThrowFileStorageExceptionWhenKeyIsBlank() {
            S3Service sut = createSut();

            FileStorageException exception = assertThrows(
                    FileStorageException.class,
                    () -> sut.storeObject("  ", VALID_CONTENT),
                    "Ожидалось исключение при пустом ключе"
            );

            assertAll("Проверка исключения при пустом ключе",
                    () -> assertEquals(EMPTY_KEY_MESSAGE, exception.getMessage(),
                            "Сообщение должно указывать на пустой ключ"),
                    () -> verifyNoInteractions(s3Client, s3Presigner)
            );
        }

        @Test
        @DisplayName("должен бросать исключение, когда данные равны null")
        void shouldThrowFileStorageExceptionWhenDataIsNull() {
            S3Service sut = createSut();

            FileStorageException exception = assertThrows(
                    FileStorageException.class,
                    () -> sut.storeObject(VALID_KEY, null),
                    "Ожидалось исключение при null содержимом"
            );

            assertAll("Проверка исключения при null содержимом",
                    () -> assertEquals(EMPTY_CONTENT_MESSAGE, exception.getMessage(),
                            "Сообщение должно указывать на пустое содержимое"),
                    () -> verifyNoInteractions(s3Client, s3Presigner)
            );
        }

        @Test
        @DisplayName("должен бросать исключение, когда данные пустые")
        void shouldThrowFileStorageExceptionWhenDataIsEmpty() {
            S3Service sut = createSut();

            FileStorageException exception = assertThrows(
                    FileStorageException.class,
                    () -> sut.storeObject(VALID_KEY, EMPTY_CONTENT),
                    "Ожидалось исключение при пустом массиве"
            );

            assertAll("Проверка исключения при пустом массиве",
                    () -> assertEquals(EMPTY_CONTENT_MESSAGE, exception.getMessage(),
                            "Сообщение должно указывать на пустое содержимое"),
                    () -> verifyNoInteractions(s3Client, s3Presigner)
            );
        }

        @Test
        @DisplayName("должен сохранять объект, когда входные данные валидны")
        void shouldStoreObjectWhenInputIsValid() throws IOException {
            S3Service sut = createSut();
            ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
            ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);

            sut.storeObject(VALID_KEY, VALID_CONTENT);

            verify(s3Client).putObject(requestCaptor.capture(), bodyCaptor.capture());
            verifyNoMoreInteractions(s3Client, s3Presigner);

            PutObjectRequest capturedRequest = requestCaptor.getValue();
            RequestBody capturedBody = bodyCaptor.getValue();
            byte[] storedBytes;
            try (InputStream stream = capturedBody.contentStreamProvider().newStream()) {
                storedBytes = stream.readAllBytes();
            }

            assertAll("Проверка параметров запроса на сохранение",
                    () -> assertEquals(BUCKET_NAME, capturedRequest.bucket(),
                            "Запрос должен содержать корректное имя bucket"),
                    () -> assertEquals(VALID_KEY, capturedRequest.key(),
                            "Запрос должен содержать исходный ключ"),
                    () -> assertArrayEquals(VALID_CONTENT, storedBytes,
                            "В хранилище должны передаваться исходные байты")
            );
        }

        @Test
        @DisplayName("должен бросать исключение, когда клиент S3 не сохраняет объект")
        void shouldThrowFileStorageExceptionWhenS3ClientFailsToStoreObject() {
            S3Service sut = createSut();
            SdkException sdkException = mock(SdkException.class);
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenThrow(sdkException);

            FileStorageException exception = assertThrows(
                    FileStorageException.class,
                    () -> sut.storeObject(VALID_KEY, VALID_CONTENT),
                    "Ожидалось пробрасывание исключения при ошибке S3"
            );

            verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
            verifyNoMoreInteractions(s3Client, s3Presigner);

            assertAll("Проверка данных исключения при ошибке сохранения",
                    () -> assertEquals(SAVE_ERROR_PREFIX + VALID_KEY, exception.getMessage(),
                            "Сообщение должно содержать ключ объекта"),
                    () -> assertEquals(sdkException, exception.getCause(),
                            "Причина исключения должна ссылаться на исходную ошибку")
            );
        }
    }

    @Nested
    @DisplayName("removeObject")
    class RemoveObject {

        @Test
        @DisplayName("должен бросать исключение, когда ключ пустой")
        void shouldThrowFileStorageExceptionWhenKeyIsBlank() {
            S3Service sut = createSut();

            FileStorageException exception = assertThrows(
                    FileStorageException.class,
                    () -> sut.removeObject(""),
                    "Ожидалось исключение при пустом ключе"
            );

            assertAll("Проверка исключения при пустом ключе удаления",
                    () -> assertEquals(EMPTY_KEY_MESSAGE, exception.getMessage(),
                            "Сообщение должно указывать на пустой ключ"),
                    () -> verifyNoInteractions(s3Client, s3Presigner)
            );
        }

        @Test
        @DisplayName("должен удалять объект, когда входные данные валидны")
        void shouldRemoveObjectWhenInputIsValid() {
            S3Service sut = createSut();
            ArgumentCaptor<DeleteObjectRequest> requestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);

            sut.removeObject(VALID_KEY);

            verify(s3Client).deleteObject(requestCaptor.capture());
            verifyNoMoreInteractions(s3Client, s3Presigner);

            DeleteObjectRequest capturedRequest = requestCaptor.getValue();
            assertAll("Проверка параметров запроса на удаление",
                    () -> assertEquals(BUCKET_NAME, capturedRequest.bucket(),
                            "Запрос должен содержать корректное имя bucket"),
                    () -> assertEquals(VALID_KEY, capturedRequest.key(),
                            "Запрос должен содержать ключ удаляемого объекта")
            );
        }

        @Test
        @DisplayName("должен бросать исключение, когда клиент S3 не удаляет объект")
        void shouldThrowFileStorageExceptionWhenS3ClientFailsToRemoveObject() {
            S3Service sut = createSut();
            SdkException sdkException = mock(SdkException.class);
            doThrow(sdkException).when(s3Client).deleteObject(any(DeleteObjectRequest.class));

            FileStorageException exception = assertThrows(
                    FileStorageException.class,
                    () -> sut.removeObject(VALID_KEY),
                    "Ожидалось пробрасывание исключения при ошибке удаления"
            );

            verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
            verifyNoMoreInteractions(s3Client, s3Presigner);

            assertAll("Проверка данных исключения при ошибке удаления",
                    () -> assertEquals(DELETE_ERROR_PREFIX + VALID_KEY, exception.getMessage(),
                            "Сообщение должно содержать ключ объекта"),
                    () -> assertEquals(sdkException, exception.getCause(),
                            "Причина исключения должна ссылаться на исходную ошибку")
            );
        }
    }

    @Nested
    @DisplayName("generatePresignedUrl")
    class GeneratePresignedUrl {

        @Test
        @DisplayName("должен бросать исключение, когда ключ пустой")
        void shouldThrowFileStorageExceptionWhenKeyIsBlank() {
            S3Service sut = createSut();

            FileStorageException exception = assertThrows(
                    FileStorageException.class,
                    () -> sut.generatePresignedUrl("\t"),
                    "Ожидалось исключение при пустом ключе"
            );

            assertAll("Проверка исключения при пустом ключе пресайна",
                    () -> assertEquals(EMPTY_KEY_MESSAGE, exception.getMessage(),
                            "Сообщение должно указывать на пустой ключ"),
                    () -> verifyNoInteractions(s3Client, s3Presigner)
            );
        }

        @Test
        @DisplayName("должен возвращать presigned URL, когда входные данные валидны")
        void shouldReturnPresignedUrlWhenInputIsValid() {
            S3Service sut = createSut();
            PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
            when(presignedRequest.url()).thenReturn(PRESIGNED_URL_OBJECT);
            when(presignedRequest.expiration()).thenReturn(PRESIGNED_EXPIRATION);
            when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedRequest);
            ArgumentCaptor<GetObjectPresignRequest> presignCaptor =
                    ArgumentCaptor.forClass(GetObjectPresignRequest.class);

            String result = sut.generatePresignedUrl(VALID_KEY);

            verify(s3Presigner).presignGetObject(presignCaptor.capture());
            verifyNoMoreInteractions(s3Client, s3Presigner);

            GetObjectPresignRequest capturedRequest = presignCaptor.getValue();
            GetObjectRequest getObjectRequest = capturedRequest.getObjectRequest();

            assertAll("Проверка параметров пресайна",
                    () -> assertEquals(PRESIGNED_URL, result,
                            "Метод должен вернуть URL, полученный от пресайнера"),
                    () -> assertEquals(BUCKET_NAME, getObjectRequest.bucket(),
                            "Запрос должен содержать корректный bucket"),
                    () -> assertEquals(VALID_KEY, getObjectRequest.key(),
                            "Запрос должен содержать ключ объекта"),
                    () -> assertEquals(URL_EXPIRATION, capturedRequest.signatureDuration(),
                            "Продолжительность действия ссылки должна совпадать с настройками")
            );
        }

        @Test
        @DisplayName("должен бросать исключение, когда пресайнер не создаёт ссылку")
        void shouldThrowFileStorageExceptionWhenPresignerFails() {
            S3Service sut = createSut();
            SdkException sdkException = mock(SdkException.class);
            when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenThrow(sdkException);

            FileStorageException exception = assertThrows(
                    FileStorageException.class,
                    () -> sut.generatePresignedUrl(VALID_KEY),
                    "Ожидалось пробрасывание исключения при ошибке пресайнера"
            );

            verify(s3Presigner).presignGetObject(any(GetObjectPresignRequest.class));
            verifyNoMoreInteractions(s3Client, s3Presigner);

            assertAll("Проверка данных исключения при ошибке пресайна",
                    () -> assertEquals(PRESIGN_ERROR_PREFIX + VALID_KEY, exception.getMessage(),
                            "Сообщение должно содержать ключ объекта"),
                    () -> assertEquals(sdkException, exception.getCause(),
                            "Причина исключения должна ссылаться на исходную ошибку")
            );
        }
    }
}
