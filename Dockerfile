FROM eclipse-temurin:25-jdk-alpine@sha256:30d9f87d702c2c1c601ed0d31e0c88ea1ea474ee7676cda7b7a59e759181c4dd AS build

ARG GRADLE_USER_HOME=/home/gradle/.gradle
ENV GRADLE_USER_HOME=${GRADLE_USER_HOME}

WORKDIR /workspace

COPY gradlew build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon

COPY src ./src

RUN ./gradlew bootJar --no-daemon -x test \
    && jar="$(find build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' | head -n 1)" \
    && test -n "${jar}" \
    && cp "${jar}" app.jar

FROM eclipse-temurin:25-jre-alpine@sha256:c707c0d18cb9e8556380719f80d96a7529d0746fbb42143893949b98ed2f8943

WORKDIR /app

COPY --from=build /workspace/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
