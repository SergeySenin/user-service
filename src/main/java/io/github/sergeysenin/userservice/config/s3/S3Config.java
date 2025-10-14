package io.github.sergeysenin.userservice.config.s3;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/*
S3Config — создаёт бины S3Client и S3Presigner с path-style и статическими учётными данными,
используя значения из S3Properties; закрывает их по завершении работы.

Зависит от S3Properties (endpoint, ключи, регион).

s3Client(), s3Presigner() конфигурируют клиентов AWS SDK v2.
 */
@Configuration
public class S3Config {

    @Bean(destroyMethod = "close")
    public S3Client s3Client(S3Properties properties) {
        // Реализация метода
        return null;
    }

    @Bean(destroyMethod = "close")
    public S3Presigner s3Presigner(S3Properties properties) {
        // Реализация метода
        return null;
    }
}
