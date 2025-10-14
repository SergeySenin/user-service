package io.github.sergeysenin.userservice.service.s3;

import io.github.sergeysenin.userservice.config.s3.S3Properties;
import io.github.sergeysenin.userservice.exception.type.FileStorageException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

/*
S3Service — обёртка над AWS SDK v2 для загрузки,
удаления и генерации presigned URL в бакете из настроек; при сбоях выбрасывает FileStorageException.

Получает S3Client, S3Presigner и S3Properties через конструктор.

uploadFile(), deleteFile(), generatePresignedUrl() выполняют операции в Minio/S3.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties properties;

    public void uploadFile(String key, byte[] content, String contentType) {
        if (content == null) {
            throw new FileStorageException("Переданы пустые данные файла");
        }
        String resolvedContentType = contentType;
        if (resolvedContentType == null || resolvedContentType.isBlank()) {
            resolvedContentType = "application/octet-stream";
        }
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(properties.bucketName())
                    .key(key)
                    .contentType(resolvedContentType)
                    .contentLength((long) content.length)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(content));
        } catch (S3Exception ex) {
            log.error("Ошибка при загрузке файла '{}' в бакет '{}'", key, properties.bucketName(), ex);
            throw new FileStorageException("Не удалось загрузить файл в хранилище", ex);
        }
    }

    public void deleteFile(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(properties.bucketName())
                    .key(key)
                    .build();
            s3Client.deleteObject(request);
        } catch (S3Exception ex) {
            log.error("Ошибка при удалении файла '{}' из бакета '{}'", key, properties.bucketName(), ex);
            throw new FileStorageException("Не удалось удалить файл из хранилища", ex);
        }
    }

    public String generatePresignedUrl(String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(properties.bucketName())
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(properties.urlExpiration())
                    .getObjectRequest(getObjectRequest)
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (S3Exception ex) {
            log.error("Ошибка при генерации presigned URL для файла '{}'", key, ex);
            throw new FileStorageException("Не удалось сгенерировать ссылку для скачивания файла", ex);
        }
    }
}
