package io.github.sergeysenin.userservice.service.s3;

import io.github.sergeysenin.userservice.config.s3.S3Properties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

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

    public void uploadFile() {
        // Реализация метода
    }

    public void deleteFile() {
        // Реализация метода
    }

    public String generatePresignedUrl(String key) {
        // Реализация метода
        return key; // Заглушка
    }
}
