# User Service

Сервис управления пользователями на Spring Boot 3.5 и Java 17.
Приложение использует PostgreSQL, Redis, S3-совместимое хранилище и интеграции по REST.
Ниже собраны инструкции по запуску, настройке профилей и тестированию.

## Содержание
- [Быстрый старт](#быстрый-старт)
  - [Локальная разработка (`local`)](#локальная-разработка-local)
  - [Контейнер для `prod`](#контейнер-для-prod)
- [Архитектура и код](#архитектура-и-код)
  - [Модель данных](#модель-данных)
- [Профили конфигурации](#профили-конфигурации)
- [Переменные окружения `prod`](#переменные-окружения-prod)
- [Конфигурация и параметры](#конфигурация-и-параметры)
  - [Параметры аватаров](#параметры-аватаров)
  - [Настройки S3](#настройки-s3)
  - [Логирование](#логирование)
- [Офлайн-прогрев](#офлайн-прогрев)
- [Тестирование](#тестирование)
- [OpenAPI и Swagger UI](#openapi-и-swagger-ui)
- [Обработка ошибок](#обработка-ошибок)
- [Формат ответа `ErrorResponse`](#формат-ответа-errorresponse)
- [Основные коды ошибок](#основные-коды-ошибок)
- [Полезные файлы](#полезные-файлы)
- [Проверка зависимостей Gradle](#проверка-зависимостей-gradle)
- [Как обновлять metadata](#как-обновлять-metadata)
- [Ограничения](#ограничения)

## Быстрый старт

### Локальная разработка (`local`)
1. Установите Docker и Docker Compose v2.
2. Выполните `docker compose up -d` в корне репозитория — поднимутся PostgreSQL, Redis и MinIO
   с зафиксированными образами, healthcheck’ами и предварительно созданным бакетом `corpbucket`.
   【F:docker-compose.yml†L1-L75】
3. Запустите приложение командой `./gradlew bootRun`.
   Профиль `local` активируется автоматически.
4. Проверяйте здоровье сервиса по адресу `http://localhost:8080/api/v1/actuator/health`.
   Консоль MinIO доступна на `http://localhost:9001` (логин/пароль `local/localpassword`).

Параметры локального окружения заданы в `src/main/resources/application-local.yaml`, инфраструктура описана
в `docker-compose.yml`.【F:src/main/resources/application-local.yaml†L5-L34】【F:docker-compose.yml†L1-L75】

### Контейнер для `prod`
1. Соберите образ: `docker build -t user-service .`.
   Мультистейдж-сборка упакует `bootJar`, настроит
   JRE-слой и добавит healthcheck.【F:Dockerfile†L1-L33】
2. Запустите контейнер, передав переменные окружения из раздела
   [«Переменные окружения `prod`»](#переменные-окружения-prod) и пробросив
   порт `8080`, например: `docker run -p 8080:8080 --env-file .env user-service`.
3. Healthcheck внутри образа обращается к `/api/v1/actuator/health/readiness`,
   поэтому его же следует мониторить в оркестраторе.【F:Dockerfile†L25-L30】

Внутри контейнера профиль `prod` активируется переменной окружения `SPRING_PROFILES_ACTIVE=prod`
(см. `Dockerfile`), а параметры JVM задаются через `JAVA_TOOL_OPTIONS`.【F:Dockerfile†L19-L24】

## Архитектура и код

### Модель данных
- **Liquibase-миграции.** Базовый changeset `user_V001__insert_users.sql` формирует таблицы пользователей и аватаров,
  настраивает внешние ключи и добавляет демонстрационные записи для профилей `local` и `test`. Контекстно-зависимые
  TRUNCATE/INSERT выполняются только вне `prod`, поэтому боевое окружение стартует с пустыми таблицами.
  【F:src/main/resources/db/changelog/changeset/user_V001__insert_users.sql†L1-L77】

## Профили конфигурации

| Профиль | Назначение                                | Источник данных                                                               | Liquibase                                                          | Redis                                          | Особенности                                                          |
|---------|-------------------------------------------|-------------------------------------------------------------------------------|--------------------------------------------------------------------|------------------------------------------------|----------------------------------------------------------------------|
| `local` | Локальная разработка через docker-compose | `jdbc:postgresql://localhost:5432/user_service`, пользователь `user/password` | Включён, ожидает `classpath:db/changelog/db.changelog-master.yaml` | `localhost:6379`                               | MinIO и внешние клиенты работают на локальные заглушки               |
| `prod`  | Боевое окружение                          | Переменные `DB_URL`, `DB_USER`, `DB_PASSWORD`                                 | Включён, ожидает `classpath:db/changelog/db.changelog-master.yaml` | Настраивается через `REDIS_HOST`, `REDIS_PORT` | Обязательные параметры клиентов и S3 берутся из переменных окружения |
| `test`  | Интеграционные тесты (Testcontainers)     | JDBC-URL подменяется контейнером PostgreSQL                                   | Отключён                                                           | Автоконфигурация Redis исключена               | Баннер выключен, логирование снижено                                 |

## Переменные окружения `prod`

| Категория       | Переменные                                                                                                                                                                 | Назначение                                                                                     |
|-----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------|
| База данных     | `DB_URL`, `DB_USER`, `DB_PASSWORD`                                                                                                                                         | JDBC-строка подключения и учётные данные                                                       |
| Redis           | `REDIS_HOST`, `REDIS_PORT` (опционально)                                                                                                                                   | Хост и порт кеша, по умолчанию `redis:6379`                                                    |
| S3              | `S3_ENDPOINT`, `S3_ACCESS_KEY`, `S3_SECRET_KEY`, `S3_BUCKET`, `S3_REGION`, `S3_URL_EXPIRATION`                                                                             | Настройка S3-совместимого хранилища, региона (`us-east-1` по умолчанию) и TTL presigned-ссылок |
| Внешние сервисы | `PROJECT_SVC_URL`, `PAYMENT_SVC_URL`                                                                                                                                       | Базовые URL интеграций                                                                         |
| Аватары         | `AVATAR_STORAGE_PATH`, `AVATAR_THUMBNAIL_MAX_SIDE`, `AVATAR_PROFILE_MAX_SIDE`,<br>`AVATAR_ALLOWED_MIME_TYPE_1`, `AVATAR_ALLOWED_MIME_TYPE_2`, `AVATAR_ALLOWED_MIME_TYPE_3` | Переопределение параметров хранения и валидации загрузок                                       |

Все переменные заданы в `src/main/resources/application-prod.yaml`: обязательные
отмечены оператором `:?`, остальные имеют дефолты. Redis по умолчанию обращается
к сервису `redis` из docker-compose, а URL сторонних сервисов и ключи доступа к S3
обязательны для прод-окружения.【F:src/main/resources/application-prod.yaml†L5-L33】

Liquibase использует master-changelog `db/changelog/db.changelog-master.yaml`,
поэтому убедитесь, что файл доступен в classpath при запуске контейнера.
【F:src/main/resources/application-prod.yaml†L13-L15】

## Конфигурация и параметры

### Параметры аватаров
Настройки `user.avatar` задаются в `application.yaml` и переопределяются переменными окружения
в `application-prod.yaml`. Они определяют базовый префикс ключей в S3, габариты превью/профильной
версии и список допустимых MIME-типов. Класс `AvatarProperties` нормализует значения, гарантирует
ненулевой список MIME и предоставляет дефолты (storage path, JPEG/PNG/WebP, размеры 170 и 1080 пикселей).
【F:src/main/resources/application-local.yaml†L23-L34】
【F:src/main/resources/application-prod.yaml†L21-L28】
【F:src/main/java/io/github/sergeysenin/userservice/config/avatar/AvatarProperties.java†L17-L105】
【F:docker-compose.yml†L7-L75】

### Настройки S3
Секция `services.s3` описывает подключение к MinIO/AWS S3: endpoint, ключи доступа, bucket и время жизни presigned URL.
Профиль `local` направляет на MinIO из `docker-compose.yml`, `prod` требует обязательные переменные. 
В коде значения биндятся в `S3Properties`, где нормализуется регион и задаётся дефолтное время истечения `PT120H`.
【F:src/main/resources/application-local.yaml†L23-L34】
【F:src/main/resources/application-prod.yaml†L21-L28】
【F:src/main/java/io/github/sergeysenin/userservice/config/s3/S3Properties.java†L13-L72】
【F:docker-compose.yml†L7-L75】

### Логирование
`logback-spring.xml` настраивает асинхронный вывод в консоль с профилями `local`, `prod` и `test`.
В `local` включён детальный DEBUG для пакета приложения и WARN для Spring/Hibernate, тогда как
`prod` ограничивается INFO. Очередь асинхронного аппендера увеличена до 1024 и настроена на
неблокирующий режим, чтобы не тормозить обработку запросов.【F:src/main/resources/logback-spring.xml†L1-L45】

## Офлайн-прогрев

Скрипт `./install.sh` готовит окружение для работы без доступа к интернету:
- проверяет наличие Docker и Java, выводит их версии;
- подготавливает Gradle Wrapper и прогревает зависимости
  (включая Testcontainers) через сборку с тестами;
- скачивает заранее pinned Docker-образы PostgreSQL, Redis, Temurin JDK/JRE
  и служебные образы Testcontainers (`testcontainers/ryuk`, `alpine`).
  При включённом флаге `PULL_MINIO` дополнительно подтягивает MinIO и `mc`.

Теги можно переопределить через переменные окружения (`POSTGRES_TAG`, `REDIS_TAG`, `TEMURIN_JDK_TAG`, `TEMURIN_JRE_TAG`,
`MINIO_TAG`, `MC_TAG`), а флаг `PULL_MINIO=0` отключает загрузку MinIO, если она не требуется для офлайновой работы.
【F:install.sh†L16-L29】【F:install.sh†L75-L89】

После выполнения скрипта можно запускать Gradle с
флагом `--offline` и использовать локальные образы.【F:install.sh†L1-L96】

## Тестирование
- Интеграционный smoke-тест `DatabaseSmokeIt` поднимает PostgreSQL 16.3
в Testcontainers и выполняет `select 1`, проверяя корректность `DataSource`.
- Gradle настроен на запуск тестов в профиле `test` с подробными логами стандартных потоков.

Команда запуска: `./gradlew test`. При необходимости предварительно выполните офлайн-прогрев.

## OpenAPI и Swagger UI
Благодаря зависимости `springdoc-openapi-starter-webmvc-ui` после запуска сервиса
Swagger UI доступен по адресу `http://localhost:8080/api/v1/swagger-ui.html`.
Используйте его для изучения и ручного тестирования REST-эндпоинтов.

## Обработка ошибок

В приложении действует единый глобальный обработчик исключений
(`GlobalExceptionHandler`), который формирует ответы в формате `ErrorResponse`.
Он используется как для стандартных ошибок Spring (`BindException`,
`MethodArgumentNotValidException`, `ConstraintViolationException`),
так и для кастомных потомков `BaseServiceException`.

### Формат ответа `ErrorResponse`

| Поле        | Тип                   | Описание                                              |
|-------------|-----------------------|-------------------------------------------------------|
| `code`      | `String`              | Стабильный идентификатор ошибки (`ErrorCode#getCode`) |
| `message`   | `String`              | Сообщение для клиента                                 |
| `timestamp` | `Instant`             | Момент формирования ответа                            |
| `details`   | `Map<String, String>` | Дополнительные сведения (например, ошибки полей)      |

Пример ответа для ошибки валидации:

```json
{
  "code": "USR-1001",
  "message": "Данные не прошли валидацию",
  "timestamp": "2024-05-01T12:34:56.789Z",
  "details": {
    "email": "must be a well-formed email address"
  }
}
```

### Основные коды ошибок

| Код        | HTTP-статус | Назначение                                                   |
|------------|-------------|--------------------------------------------------------------|
| `USR-1000` | 400         | Ошибки биндинга запроса (`BindException`)                    |
| `USR-1001` | 422         | Нарушение бизнес-валидации входных данных                    |
| `USR-1002` | 422         | Ошибки загрузки аватара                                      |
| `USR-2001` | 404         | Сущность не найдена (`EntityNotFoundException`)              |
| `USR-2002` | 404         | Пользователь не найден                                       |
| `USR-2003` | 404         | Аватар не найден                                             |
| `USR-3000` | 409         | Нарушение ограничений целостности (например, уникальность)   |
| `USR-7000` | 500         | Ошибки файлового хранилища                                   |
| `USR-9000` | 500         | Неперехваченные исключения (`RuntimeException`, `Exception`) |

Для внедрения новых бизнес-ошибок создавайте собственные классы,
наследуясь от `BaseServiceException`, и указывайте подходящий `ErrorCode`.
В этом случае обработчик автоматически сформирует ответ в нужном формате.

## Полезные файлы
- `docker-compose.yml` — локальная инфраструктура для профиля `local`: PostgreSQL, Redis, MinIO
  и `mc` создают бакет `corpbucket` с публичной политикой доступа.【F:docker-compose.yml†L7-L75】
- `Dockerfile` — мультистейдж-сборка образа с разделением на build/runtime, 
  настройкой профиля `prod`, healthcheck и отдельным пользователем `app`.【F:Dockerfile†L1-L33】
- `install.sh` — сценарий офлайн-прогрева: проверяет инструменты, прогревает Gradle
  и скачивает pinned Docker-образы для приложения и тестов.【F:install.sh†L1-L96】
- `src/main/resources/application-*.yaml` — базовые настройки приложения 
  и профилей `local`/`prod`, включая параметры Redis, метрик и аватаров;
  `src/test/resources/application-test.yaml` — конфигурация профиля `test` для интеграционных тестов.
  【F:src/main/resources/application.yaml†L1-L48】
  【F:src/main/resources/application-local.yaml†L5-L34】
  【F:src/main/resources/application-prod.yaml†L5-L45】
  【F:src/test/resources/application-test.yaml†L1-L21】
- `docs/tech-debt-log.md` — журнал технического долга;
  `docs/unit-test-guidelines.md` — правила написания юнит-тестов в проекте.
  【F:docs/tech-debt-log.md†L1-L55】
  【F:docs/unit-test-guidelines.md†L1-L120】

## Проверка зависимостей Gradle

В проекте включена строгая проверка зависимостей Gradle (`verification-metadata.xml`).
Каждая сборка сверяет контрольные суммы артефактов из
конфигураций `compileClasspath` и `runtimeClasspath`, поэтому
любые изменения дерева зависимостей требуют обновления metadata.

### Как обновлять metadata

1. Внесите необходимые изменения в `build.gradle.kts` или `settings.gradle.kts`
   (добавление/удаление зависимостей, изменение версий, глобальные `exclude`).
2. Выполните команду для вашей среды.

   **Linux/macOS (bash/zsh)**

   ```bash
   ./gradlew --write-verification-metadata sha256
   ```

   При необходимости принудительно обновить артефакты перед
   генерацией metadata добавьте `--refresh-dependencies`:

   ```bash
   ./gradlew --write-verification-metadata sha256 --refresh-dependencies
   ```

   **Windows (PowerShell)**

   ```powershell
   .\gradlew `
     --write-verification-metadata sha256
   ```

   С обновлением зависимостей:

   ```powershell
   .\gradlew `
     --write-verification-metadata sha256 `
     --refresh-dependencies
   ```

   **Windows (cmd.exe)**

   ```cmd
   gradlew --write-verification-metadata sha256
   ```

   С обновлением зависимостей:

   ```cmd
   gradlew --write-verification-metadata sha256 --refresh-dependencies
   ```

   Gradle пересоберёт `gradle/verification-metadata.xml`,
   добавив контрольные суммы новых артефактов.
3. Проверьте, что файл обновился автоматически, и 
   закоммитьте его вместе с правками зависимостей.

### Ограничения

- Не используйте динамические версии (`+`, `latest.release` и т.п.)
  — они ломают воспроизводимость и влекут ошибки верификации.
- Старайтесь ограничивать `exclude` только нужными конфигурациями,
  чтобы не вызывать массовых обновлений metadata.
- Не редактируйте `verification-metadata.xml` вручную: любые изменения
  вносите только через команду `--write-verification-metadata`.
- При необходимости полной очистки окружения остановите демоны Gradle
  (`./gradlew --stop`) и удалите каталоги `.gradle/` и `~/.gradle/caches`.
