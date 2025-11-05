#!/usr/bin/env bash
# install.sh — одноразовый онлайн-прогрев для офлайна.
# Назначение:
#   1) Прогреть Gradle wrapper и кэш зависимостей, создать файл верификации артефактов.
#   2) Выполнить сборку с тестами, чтобы Testcontainers скачал служебные образы.
#   3) Явно скачать Docker-образы, используемые в проекте и в тестах.
# Когда запускать: один раз при наличии сети перед офлайн-работой.
# Что НЕ делает: не меняет код, не публикует артефакты, не настраивает Docker daemon.

set -euo pipefail

### ---------- Конфигурация: пины версий и флаги ----------
# Пины заданы как readonly, но допускают переопределение через переменные окружения.
# Пример: POSTGRES_IMAGE=18.1 ./install.sh

# Образы сервисов из docker-compose
readonly POSTGRES_IMAGE="${POSTGRES_IMAGE:-18.0}"
readonly REDIS_IMAGE="${REDIS_IMAGE:-8.2.3-alpine}"
readonly KEYCLOAK_IMAGE="${KEYCLOAK_IMAGE:-quay.io/keycloak/keycloak:26.4.2}"
readonly KAFKA_IMAGE="${KAFKA_IMAGE:-bitnamilegacy/kafka:4.0.0-debian-12-r10}"
readonly MINIO_IMAGE="${MINIO_IMAGE:-RELEASE.2025-09-07T16-13-09Z}"
readonly MC_IMAGE="${MC_IMAGE:-RELEASE.2025-08-13T08-35-41Z}"

# Образы JVM для сборки и рантайма
readonly TEMURIN_JDK_TAG="${TEMURIN_JDK_TAG:-17-jdk-alpine}"
readonly TEMURIN_JRE_TAG="${TEMURIN_JRE_TAG:-17-jre-alpine}"

# Флаги прогрева опциональных сервисов (1=качать, 0=пропустить)
readonly PULL_KEYCLOAK="${PULL_KEYCLOAK:-1}"
readonly PULL_KAFKA="${PULL_KAFKA:-1}"
readonly PULL_MINIO="${PULL_MINIO:-1}"

# Testcontainers: служебные образы, скачиваются библиотекой под капотом
readonly RYUK_IMAGE="${RYUK_IMAGE:-testcontainers/ryuk:0.6.0}"
readonly TINY_IMAGE="${TINY_IMAGE:-alpine:3.18}"

# Поведение Gradle
readonly GRADLE_NO_DAEMON="--no-daemon"

### ---------- Утилиты ----------
require() { command -v "$1" >/dev/null 2>&1 || { echo "Требуется '$1' в PATH"; exit 1; }; }
log() { printf '[%s] %s\n' "$(date -Iseconds)" "$*"; }
pull() { local img="$1"; log "docker pull ${img}"; docker pull "${img}"; }

### ---------- Предпроверки окружения ----------
require bash
require docker
require chmod

# Docker daemon должен быть доступен
if ! docker info >/dev/null 2>&1; then
  echo "Docker daemon недоступен. Запустите Docker Desktop/службу docker."
  exit 1
fi

if [ ! -x ./gradlew ]; then
  require java
fi

log "Версии инструментов:"
docker --version || true
java -version 2>/dev/null || true

### ---------- Этап 1: Gradle wrapper и верификация зависимостей ----------
log "Подготовка Gradle wrapper"
chmod +x ./gradlew
./gradlew ${GRADLE_NO_DAEMON} --version

# Dependency Verification: фиксирует SHA-256 всех загружаемых артефактов
if [ ! -f "gradle/verification-metadata.xml" ]; then
  log "Генерация gradle/verification-metadata.xml (SHA-256)"
  ./gradlew ${GRADLE_NO_DAEMON} help --write-verification-metadata sha256
fi

### ---------- Этап 2: Полная сборка с тестами (онлайн-прогрев) ----------
# Цель: заполнить локальный кэш зависимостей и дать Testcontainers один раз скачать служебные образы
log "Сборка проекта и тесты (онлайн прогрев)"
./gradlew ${GRADLE_NO_DAEMON} clean test bootJar

### ---------- Этап 3: Явный прогрев Docker-образов ----------
log "Загрузка основных образов"
pull "postgres:${POSTGRES_IMAGE}"
pull "redis:${REDIS_IMAGE}"

# Опциональные сервисы управляются через флаги PULL_*
if [ "${PULL_KEYCLOAK}" = "1" ]; then
  pull "${KEYCLOAK_IMAGE}"
else
  log "Пропущена загрузка образа Keycloak (PULL_KEYCLOAK=0)"
fi

if [ "${PULL_KAFKA}" = "1" ]; then
  pull "${KAFKA_IMAGE}"
else
  log "Пропущена загрузка образа Kafka (PULL_KAFKA=0)"
fi

if [ "${PULL_MINIO}" = "1" ]; then
  log "Загрузка образов MinIO для локального S3"
  pull "minio/minio:${MINIO_IMAGE}" || true
  pull "minio/mc:${MC_IMAGE}" || true
else
  log "Пропущена загрузка образов MinIO (PULL_MINIO=0)"
fi

pull "eclipse-temurin:${TEMURIN_JDK_TAG}"
pull "eclipse-temurin:${TEMURIN_JRE_TAG}"

log "Загрузка служебных образов Testcontainers"
pull "${RYUK_IMAGE}"
pull "${TINY_IMAGE}"

### ---------- Итог ----------
log "Готово. Кэш Gradle заполнен. Docker-образы скачаны."
log "Офлайн-режим: используйте ' ./gradlew --offline test ' и локальные образы без сети."
log "Если файл прав не зафиксирован в git:  git add --chmod=+x install.sh && git commit -m 'install.sh exec'"
