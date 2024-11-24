# Этап сборки
FROM gradle:8.11-jdk17 AS build

# Установка аргумента для Gradle-кэша
ARG GRADLE_USER_HOME=/home/gradle/.gradle
ENV GRADLE_USER_HOME=${GRADLE_USER_HOME}

# Установка рабочей директории
WORKDIR /home/gradle/project

# Копирование только файлов с зависимостями для кэширования
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# Загрузка зависимостей без сборки проекта
RUN gradle dependencies --no-daemon

# Копирование остальных файлов проекта
COPY src ./src

# Сборка проекта
RUN gradle build --no-daemon

# Этап выполнения
FROM openjdk:17-jdk-alpine

# Установка рабочей директории
WORKDIR /app

# Копирование скомпилированного JAR из предыдущего этапа
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

# Указание порта приложения (например, 8080)
EXPOSE 8080

# Команда для запуска приложения
ENTRYPOINT ["java","-jar","/app/app.jar"]
