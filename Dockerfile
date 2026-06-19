# Этап сборки
FROM gradle:8.11-jdk17@sha256:91d559b8d55f522de5bc6882f73bcedc4e2cc7b0a58e839a9fa0ed95811a988d AS build

# Установка аргумента для Gradle-кэша
ARG GRADLE_USER_HOME=/home/gradle/.gradle
ENV GRADLE_USER_HOME=${GRADLE_USER_HOME}

# Установка рабочей директории
WORKDIR /home/gradle/project
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

# Копирование только файлов с зависимостями для кэширования
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# Загрузка зависимостей без сборки проекта
RUN gradle dependencies --no-daemon

# Копирование остальных файлов проекта
COPY src ./src

# Сборка исполняемого JAR; тесты запускаются отдельным CI/локальным шагом.
RUN gradle bootJar --no-daemon -x test \
    && jar="$(find build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' | head -n 1)" \
    && test -n "${jar}" \
    && cp "${jar}" app.jar

# Этап выполнения
FROM eclipse-temurin:17-jdk-alpine@sha256:5d14725f0e49e19df217f6ce179039f01ca25f5f9aa958573b467312599ca246

# Установка рабочей директории
WORKDIR /app

# Копирование скомпилированного JAR из предыдущего этапа
COPY --from=build /home/gradle/project/app.jar app.jar

# Указание порта приложения (например, 8080)
EXPOSE 8080

# Команда для запуска приложения
ENTRYPOINT ["java","-jar","/app/app.jar"]
