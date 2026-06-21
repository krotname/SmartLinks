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

# Ubuntu JRE (glibc) required for Shenandoah GC — Alpine/musl does not include it
FROM eclipse-temurin:25-jre@sha256:7ea65de6187ad8fbcc0ad155950c38664a7371148bb3ccf1ec1e1b286b44ad08

WORKDIR /app

COPY --from=build /workspace/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseShenandoahGC", \
  "-XX:ShenandoahGCHeuristics=adaptive", \
  "-XX:MaxGCPauseMillis=10", \
  "-jar", "/app/app.jar"]
