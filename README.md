# SmartLinks

[English](README.en.md)

[![CI](https://github.com/krotname/SmartLinks/actions/workflows/gradle.yml/badge.svg)](https://github.com/krotname/SmartLinks/actions/workflows/gradle.yml?query=branch%3Amain)
[![Coverage](.github/badges/jacoco.svg)](https://github.com/krotname/SmartLinks/actions/workflows/gradle.yml?query=branch%3Amain)
[![Docker](https://img.shields.io/badge/Docker-ghcr.io-2496ED?logo=docker&logoColor=white)](https://github.com/krotname/SmartLinks/pkgs/container/smart-links)
[![License: GPL-3.0](https://img.shields.io/badge/license-GPL--3.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-25-007396)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F)](https://spring.io/projects/spring-boot)
[![Gradle](https://img.shields.io/badge/Gradle-9.6-02303A)](https://gradle.org/)
[![Redis](https://img.shields.io/badge/Redis-8.2-DC382D)](https://redis.io/)

SmartLinks ценен не набором endpoint-ов, а тем, как устроен код: маршрутизация разложена по маленьким ролям, правила расширяются через композицию, а инфраструктура не протекает в доменную логику.

## Архитектура

- `RedirectController` принимает HTTP-запрос и делегирует сценарий сервисам.
- `RedirectResolver` отвечает за выбор итогового URI.
- `RedirectCommand` сохраняет паттерн Command для выполнения правил.
- `PredicateFactory` и отдельные `Predicate`-классы дают расширение через новые типы предикатов.
- `SmartLinkRepository` изолирует Redis за портом репозитория.
- `RedisConfig` остаётся тонким адаптером и не дублирует автоконфигурацию Spring Boot.

## Инженерные правила

- В Java-коде и тестах нет явных операторов ветвления для бизнес-выбора.
- Ветвление правил выражено через Command, Factory, Strategy и Stream API.
- Зависимости внедряются через явные конструкторы без Lombok.
- Лишние библиотеки удалены: HTTP-клиент, cloud BOM, utility-библиотека, URL validator и отдельный Redis test adapter.
- Spring Boot BOM подключён явно через Gradle platform.
- Секреты не лежат в `application.yml`; локальный Redis запускается без пароля.

## Тесты

- Unit-тесты проверяют отдельные роли: predicates, services, command chain, resolver.
- Web slice использует актуальный Boot 4 пакет `spring-boot-starter-webmvc-test`.
- Redis integration test использует официальный `redis:8.2-alpine` через `GenericContainer`.
- Test fixtures вынесены в `src/test/java/.../support`, чтобы сценарии не дублировали сборку моделей.
- JaCoCo запускается после `test` и формирует HTML-отчёт.

## Supply Chain

- Gradle Wrapper обновлён до 9.6.0 с проверкой SHA256 дистрибутива.
- GitHub Actions закреплены по SHA, рядом оставлены исходные tag-комментарии.
- Dependency graph вынесен в отдельный workflow без job-level branching.
- Docker build использует project wrapper, поэтому не зависит от задержки публикации Gradle Docker image.

## Локальная проверка

```powershell
.\gradlew.bat clean build
```

```powershell
docker compose up --build
```

API документация доступна на `/swagger-ui/index.html` после старта приложения.
