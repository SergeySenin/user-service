FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /workspace

COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle.kts build.gradle.kts ./

RUN chmod +x gradlew && ./gradlew --no-daemon --version

COPY src src

RUN ./gradlew --no-daemon clean bootJar -x test
RUN cp build/libs/*-SNAPSHOT.jar app.jar || cp build/libs/*.jar app.jar


FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app \
    && apk add --no-cache curl

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

COPY --from=builder --chown=app:app /workspace/app.jar /app/app.jar

USER app
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -sf http://localhost:8080/api/v1/actuator/health/readiness | grep -q '"status":"UP"'

ENTRYPOINT ["java","-jar","/app/app.jar"]
