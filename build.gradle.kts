import org.gradle.api.file.RegularFile
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.testing.jacoco.tasks.JacocoReportBase

plugins {
    java
    checkstyle
    id("org.springframework.boot") version "3.5.7"
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

configurations {
    compileOnly { extendsFrom(configurations.annotationProcessor.get()) }
}

dependencies {
    /**
     * Dependency Platforms (Bills Of Materials)
     */
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2025.0.0"))
    implementation(platform("software.amazon.awssdk:bom:2.37.3"))
    testImplementation(platform("org.testcontainers:testcontainers-bom:2.0.1"))

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
     * Database Driver & Migrations
     */
    implementation("org.liquibase:liquibase-core")
    runtimeOnly("org.postgresql:postgresql")

    /**
     * Caching / Redis
     */
    implementation("org.springframework.boot:spring-boot-starter-data-redis") {
        exclude(group = "io.lettuce", module = "lettuce-core")
    }
    implementation("redis.clients:jedis")

    /**
     * Security / OAuth2 Resource Server
     */
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    /**
     * Spring Cloud
     */
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    /**
     * Messaging / Kafka
     */
    implementation("org.springframework.kafka:spring-kafka")

    /**
     * Serialization / Media
     */
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv")
    implementation("net.coobird:thumbnailator:0.4.21")
    runtimeOnly("com.github.usefulness:webp-imageio:0.10.2")

    /**
     * Object Storage (AWS SDK v2)
     */
    implementation("software.amazon.awssdk:s3")

    /**
     * OpenAPI / Docs
     */
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")

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
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")

    /**
     * Testcontainers
     */
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.testcontainers:testcontainers-kafka")
    testImplementation("com.redis:testcontainers-redis:2.2.4")
}

jacoco {
    toolVersion = "0.8.14"
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.named("test"))

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    configureJacocoClassDirectories(this)
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(tasks.named("test"))

    violationRules {
        rule {
            limit {
                // Временное отключение (сделать 0.9 после первых модульных тестов).
                minimum = 0.0.toBigDecimal()
            }
        }
    }

    configureJacocoClassDirectories(this)
}

extensions.configure<CheckstyleExtension>("checkstyle") {
    toolVersion = "12.1.1"
    configDirectory.set(layout.projectDirectory.dir("config/checkstyle"))
    config = resources.text.fromFile(layout.projectDirectory.file("config/checkstyle/checkstyle.xml"))
    configProperties["checkstyle.enableExternalDtdLoad"] = "false"
    isIgnoreFailures = false
}

val checkstyleTaskNames = listOf("checkstyleMain", "checkstyleTest")
val checkstyleStylesheet = layout.projectDirectory.file("config/checkstyle/checkstyle-noframes-severity-sorted.xsl")
val checkstyleExcludePatterns = listOf("**/resources/**", "**/generated/**")

checkstyleTaskNames
    .map { tasks.named<Checkstyle>(it) }
    .forEach { task ->
        configureCheckstyleTask(task, checkstyleStylesheet, checkstyleExcludePatterns)
    }

tasks.test {
    useJUnitPlatform()
    // Вывод стандартных потоков включён — удобно видеть подробные логи тестов.
    testLogging { showStandardStreams = true }
    systemProperty("spring.profiles.active", "test")
    finalizedBy(tasks.named("jacocoTestReport"))
}

tasks.named("check") {
    dependsOn(
        tasks.named("jacocoTestCoverageVerification"),
        tasks.named("checkstyleMain"),
        tasks.named("checkstyleTest")
    )
}

fun configureJacocoClassDirectories(jacocoTask: JacocoReportBase) {
    val sourceSets = jacocoTask.project.extensions.getByType<SourceSetContainer>()
    jacocoTask.classDirectories.setFrom(
        jacocoTask.project.files(
            sourceSets.getByName("main").output.asFileTree.matching {
                exclude(
                    "**/client/**",
                    "**/config/**",
                    "**/controller/**",
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

fun configureCheckstyleTask(
    taskProvider: TaskProvider<Checkstyle>,
    stylesheet: RegularFile,
    excludePatterns: List<String>
) {
    taskProvider.configure {
        reports {
            xml.required.set(true)
            html.required.set(true)
            html.stylesheet = resources.text.fromFile(stylesheet)
        }

        excludePatterns.forEach(::exclude)
    }
}
