package io.github.sergeysenin.userservice.service.s3;

import io.github.sergeysenin.userservice.config.s3.S3Properties;
import io.github.sergeysenin.userservice.exception.type.FileStorageException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    public void storeObject(String s3ObjectKey, byte[] objectData) {
        requireKey(s3ObjectKey);
        requireData(objectData);

        try {
            PutObjectRequest request = buildPutObjectRequest(s3Properties.bucketName(), s3ObjectKey);
            s3Client.putObject(request, RequestBody.fromBytes(objectData));

            log.debug("S3 объект сохранен: bucket={}, key={}, size={} bytes",
                    s3Properties.bucketName(), s3ObjectKey, objectData.length);

        } catch (SdkException exception) {
            log.error("Сбой сохранения объекта в S3: bucket={}, key={}",
                    s3Properties.bucketName(), s3ObjectKey, exception);
            throw new FileStorageException("Не удалось сохранить файл в хранилище: " + s3ObjectKey, exception);
        }
    }

    public void removeObject(String s3ObjectKey) {
        requireKey(s3ObjectKey);

        try {
            DeleteObjectRequest request = buildDeleteObjectRequest(s3Properties.bucketName(), s3ObjectKey);
            s3Client.deleteObject(request);

            log.debug("S3 объект удален: bucket={}, key={}",
                    s3Properties.bucketName(), s3ObjectKey);

        } catch (SdkException exception) {
            log.error("Сбой удаления объекта из S3: bucket={}, key={}",
                    s3Properties.bucketName(), s3ObjectKey, exception);
            throw new FileStorageException("Не удалось удалить файл из хранилища: " + s3ObjectKey, exception);
        }
    }

    public String generatePresignedUrl(String s3ObjectKey) {
        requireKey(s3ObjectKey);

        try {
            GetObjectRequest request = buildGetObjectRequest(s3Properties.bucketName(), s3ObjectKey);
            GetObjectPresignRequest presignRequest = buildGetObjectPrRequest(request, s3Properties.urlExpiration());

            var presignedGetObject = s3Presigner.presignGetObject(presignRequest);

            log.debug("S3 presigned URL создан: bucket={}, key={}, expiresAt={}",
                    s3Properties.bucketName(), s3ObjectKey, presignedGetObject.expiration());

            return presignedGetObject.url().toString();

        } catch (SdkException exception) {
            log.error("Сбой генерации presigned URL: bucket={}, key={}",
                    s3Properties.bucketName(), s3ObjectKey, exception);
            throw new FileStorageException("Не удалось сгенерировать ссылку для файла: " + s3ObjectKey, exception);
        }
    }

    private PutObjectRequest buildPutObjectRequest(String bucket, String key) {
        return PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
    }

    private DeleteObjectRequest buildDeleteObjectRequest(String bucket, String key) {
        return DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
    }

    private GetObjectRequest buildGetObjectRequest(String bucket, String key) {
        return GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
    }

    private GetObjectPresignRequest buildGetObjectPrRequest(
            GetObjectRequest request,
            Duration duration
    ) {
        return GetObjectPresignRequest.builder()
                .signatureDuration(duration)
                .getObjectRequest(request)
                .build();
    }

    private static void requireKey(String key) {
        if (key == null || key.isBlank()) throw new FileStorageException("Пустой ключ объекта");
    }

    private static void requireData(byte[] data) {
        if (data == null || data.length == 0) throw new FileStorageException("Пустое содержимое файла");
    }
}
