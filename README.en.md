# SmartLinks

[Russian](README.md)

[![CI](https://github.com/krotname/SmartLinks/actions/workflows/gradle.yml/badge.svg)](https://github.com/krotname/SmartLinks/actions/workflows/gradle.yml?query=branch%3Amain)
[![Coverage](.github/badges/jacoco.svg)](https://github.com/krotname/SmartLinks/actions/workflows/gradle.yml?query=branch%3Amain)
[![Docker](https://img.shields.io/badge/Docker-ghcr.io-2496ED?logo=docker&logoColor=white)](https://github.com/krotname/SmartLinks/pkgs/container/smart-links)
[![License: GPL-3.0](https://img.shields.io/badge/license-GPL--3.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-25-007396)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F)](https://spring.io/projects/spring-boot)
[![Gradle](https://img.shields.io/badge/Gradle-9.6-02303A)](https://gradle.org/)
[![Redis](https://img.shields.io/badge/Redis-8.2-DC382D)](https://redis.io/)

SmartLinks is presented as an engineering sample first: routing is split into small roles, rule evaluation is composed from independent parts, and infrastructure stays behind narrow ports.

## Architecture

- `RedirectController` accepts HTTP traffic and delegates the use case.
- `RedirectResolver` owns target URI resolution.
- `RedirectCommand` keeps rule execution behind the Command pattern.
- `PredicateFactory` and dedicated `Predicate` classes keep routing rules open for extension.
- `SmartLinkRepository` keeps Redis behind a repository port.
- `RedisConfig` is a thin adapter and leaves connection setup to Spring Boot auto-configuration.

## Engineering Rules

- Java production code and tests contain no explicit branch operators for business selection.
- Rule branching is expressed through Command, Factory, Strategy, and Stream API composition.
- Dependencies are declared through explicit constructors with no Lombok.
- Removed unused libraries: HTTP client, cloud BOM, utility library, URL validator, and a dedicated Redis test adapter.
- Spring Boot dependency management is declared through an explicit Gradle platform.
- Secrets are not stored in `application.yml`; local Redis runs without a default password.

## Tests

- Unit tests target single roles: predicates, services, command chain, resolver.
- Web slice tests use the current Boot 4 `spring-boot-starter-webmvc-test` package.
- Redis integration tests use the official `redis:8.2-alpine` image through `GenericContainer`.
- Shared test fixtures live under `src/test/java/.../support`.
- JaCoCo runs after `test` and produces an HTML report.

## Supply Chain

- Gradle Wrapper is updated to 9.6.0 with distribution SHA256 validation.
- GitHub Actions are pinned by SHA and keep the original tag as a comment.
- Dependency graph submission lives in a dedicated workflow without job-level branching.
- Docker build uses the project wrapper, so it does not depend on Gradle Docker image publication lag.

## Local Checks

```powershell
.\gradlew.bat clean build
```

```powershell
docker compose up --build
```

OpenAPI UI is available at `/swagger-ui/index.html` after the application starts.
