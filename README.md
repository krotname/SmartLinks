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

## Нагрузочное тестирование

Тесты написаны на **Gatling 3.13.5** (Java DSL) и запускаются через Gradle без внешних инструментов.
Исходники — в `src/gatling/java/name/krot/smartlinks/load/`.

**Окружение**

| Параметр | Значение |
|---|---|
| Хост | Windows 11 Pro, Intel Core i7, 32 GB RAM |
| Приложение | Docker Desktop — 1 контейнер `smart-links:latest` |
| Redis | `redis:8.2-alpine` — отдельный контейнер |
| Gatling | 3.13.5, Java 25 (toolchain auto-provisioned) |
| Модель инъекции | Closed — persistent users, keep-alive соединения |

Закрытая модель (closed injection) была выбрана специально, чтобы не исчерпывать пул эфемерных портов Windows при высоком параллелизме.

### SmokeSim — дымовой тест

5 пользователей GET, 90 секунд (20 с разгон → 50 с нагрузка).

| Метрика | Значение |
|---|---|
| Всего запросов | 32 909 |
| Ошибок | 0 |
| p50 | 8 мс |
| p75 | 12 мс |
| p95 | 28 мс |
| p99 | 61 мс |
| Пропускная способность | 366 req/s |

### LoadSim — нагрузочный тест

40 пользователей GET + 10 пользователей POST, 10 минут (2 мин разгон → 5 мин нагрузка → 1 мин сброс).

| Запрос | Кол-во | p50 | p75 | p95 | p99 | req/s |
|---|---|---|---|---|---|---|
| `GET /s/{linkId}` | 532 086 | 29 мс | 43 мс | 80 мс | 169 мс | 887 |
| `POST /api/smartlinks` | 149 896 | 28 мс | 40 мс | 75 мс | 142 мс | 250 |
| **Итого** | **682 032** | **29 мс** | **42 мс** | **78 мс** | **163 мс** | **1 137** |

Ошибок: 0. Все assertion пройдены (GET p95 < 100 мс, GET p99 < 300 мс, POST p95 < 200 мс).

### StressSim — стресс-тест

Ступенчатый рост только GET: 10 → 25 → 50 → 75 → 100 → 150 → 200 пользователей (по 90 с на ступень).

| Метрика | Значение |
|---|---|
| Всего запросов | 1 343 152 |
| Ошибок | 0 |
| p50 | 36 мс |
| p75 | 56 мс |
| p95 | 100 мс |
| p99 | 1 115 мс |
| Пропускная способность | 1 722 req/s |

p99 выходит за 1 с на уровне ≥ 150 пользователей — Redis pipeline / JVM GC давление. До 100 пользователей p99 остаётся ниже 300 мс. Ошибок нет даже при 200 пользователях.

### SpikeSim — спайк-тест

10 пользователей (60 с) → рост до 100 за 2 с → удержание 120 с → возврат к 10 за 2 с → удержание 90 с.

| Метрика | Значение |
|---|---|
| Всего запросов | 738 634 |
| Ошибок | 0 |
| p50 | 25 мс |
| p75 | 34 мс |
| p95 | 52 мс |
| p99 | 125 мс |
| Пропускная способность | 2 052 req/s |

Система поглотила 10-кратный всплеск без единой ошибки. Assertion пройдены: success rate > 99 %, p99 < 1 000 мс.

### BreakSim — поиск потолка

Ступенчатый рост 500 → 1 000 → 2 000 → 3 000 → 4 000 → 5 000 пользователей (по 90 с на ступень).

| Параметр | Значение |
|---|---|
| Всего запросов | 690 009 |
| Ошибки | появляются при ≥ 4 500 пользователях |
| Ошибок на 4 500 → 5 000 | 997 (0,14 %) |
| Тип ошибки | HTTP 5xx, среднее время ответа 3,4 с (thread pool) |
| p50 (весь тест) | 534 мс |
| p95 (весь тест) | 1 189 мс |
| Пропускная способность | ~2 100–2 300 req/s (потолок) |

**Выводы:**

| Диапазон | Ошибки | p50 | p99 |
|---|---|---|---|
| ≤ 200 пользователей | 0 | 36 мс | 1 115 мс |
| 200 – 1 500 | 0 | 233 мс | 899 мс |
| 1 500 – 4 000 | 0 | ~970 мс | ~3 700 мс |
| 4 500 – 5 000 | **0,14 %** | 534 мс | 2 225 мс |

Потолок пропускной способности — ~2 100 req/s. При превышении ~4 500 персистентных соединений Tomcat исчерпывает пул потоков и начинает возвращать 5xx. До этой отметки очередь принимает все запросы без потерь.

### Запуск тестов

```powershell
# Собрать образ и поднять окружение
docker build -t smart-links:latest .
docker compose -p smartlinks-lt -f docker-compose.loadtest.yml up -d

# Запустить сценарии
.\gradlew.bat gatlingRunSmokeSim
.\gradlew.bat gatlingRunLoadSim
.\gradlew.bat gatlingRunStressSim
.\gradlew.bat gatlingRunSpikeSim
.\gradlew.bat gatlingRunBreakSim
```

HTML-отчёты сохраняются в `build/gatling-results/`.
