import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType
import org.gradle.testing.jacoco.tasks.JacocoReportBase

plugins {
    java
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    jacoco
}

group = "io.github.sergeysenin"
version = "0.0.1-SNAPSHOT"
description = "user-service"

java {
    toolchain { languageVersion = JavaLanguageVersion.of(17) }
}

repositories {
    mavenCentral()
}

jacoco {
    toolVersion = "0.8.13"
}

configurations {
    compileOnly { extendsFrom(configurations.annotationProcessor.get()) }
}

dependencies {
    /**
     * Dependency Platforms (Bills Of Materials)
     */
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2025.0.0"))
    implementation(platform("software.amazon.awssdk:bom:2.34.5"))
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.21.3"))

    /**
     * Core Starters: Observability / Validation / Web
     */
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")

    /**
     * Data Access
     */
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    /**
     * Caching / Redis
     */
    implementation("org.springframework.boot:spring-boot-starter-data-redis") {
        exclude(group = "io.lettuce", module = "lettuce-core")
    }
    implementation("redis.clients:jedis")

    /**
     * Database Driver & Migrations
     */
    implementation("org.liquibase:liquibase-core")
    runtimeOnly("org.postgresql:postgresql")

    /**
     * OpenAPI / Docs
     */
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")

    /**
     * Spring Cloud
     */
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    /**
     * Object Storage (AWS SDK v2)
     */
    implementation("software.amazon.awssdk:s3")

    /**
     * Serialization / Media
     */
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv")
    implementation("net.coobird:thumbnailator:0.4.20")

    /**
     * Developer Tooling (Annotation Processors)
     */
    val lombokVersion = "1.18.42"
    val mapstructVersion = "1.6.3"
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    implementation("org.mapstruct:mapstruct:$mapstructVersion")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    /**
     * Testing
     */
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    /**
     * Testcontainers
     */
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("com.redis.testcontainers:testcontainers-redis-junit:1.6.4")
}

tasks.test {
    useJUnitPlatform()
    // Вывод стандартных потоков включён — удобно видеть подробные логи тестов.
    testLogging { showStandardStreams = true }
    systemProperty("spring.profiles.active", "test")
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    configureJacocoClassDirectories(this)
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)

    violationRules {
        rule {
            limit {
                minimum = 0.8.toBigDecimal()
            }
        }
    }

    configureJacocoClassDirectories(this)
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

fun configureJacocoClassDirectories(jacocoTask: JacocoReportBase) {
    val sourceSets = jacocoTask.project.extensions.getByType<SourceSetContainer>()
    jacocoTask.classDirectories.setFrom(
        jacocoTask.project.files(
            sourceSets.getByName("main").output.asFileTree.matching {
                exclude(
                    "**/client/**",
                    "**/config/**",
                    "**/dto/**",
                    "**/entity/**",
                    "**/exception/**",
                    "**/mapper/**",
                    "**/repository/**",
                    "**/com/json/**",
                    "**/UserServiceApplication.*"
                )
            }
        )
    )
}
