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

## Производительность

В версии v2 применены четыре оптимизации:

| Изменение | Реализация |
|---|---|
| **Virtual threads (Project Loom)** | `spring.threads.virtual.enabled=true` — каждый HTTP-запрос получает лёгкий виртуальный поток JVM, thread pool Tomcat перестаёт быть узким местом |
| **Shenandoah GC** | `-XX:+UseShenandoahGC` в Docker ENTRYPOINT (Ubuntu JRE — glibc обязателен для Shenandoah) |
| **Redis JSON вместо JDK serialization** | `StringRedisTemplate` + `JsonMapper` (Jackson 3, `tools.jackson`) + индивидуальные ключи `sl:<id>` вместо единого хэша |
| **Lettuce connection pool** | `spring.data.redis.lettuce.pool.enabled=true`, 16 соединений — виртуальные потоки не блокируются на одном Lettuce-соединении |

Spring Boot 4.1 не публикует `spring-boot-starter-undertow` — Undertow убран из дистрибутива.
Virtual threads с Tomcat 11 дают те же или лучшие результаты.

## Нагрузочное тестирование

Тесты написаны на **Gatling 3.13.5** (Java DSL) и запускаются через Gradle без внешних инструментов.
Исходники — в `src/gatling/java/name/krot/smartlinks/load/`.

**Окружение**

| Параметр | Значение |
|---|---|
| Хост | Windows 11 Pro, Intel Core i7, 32 GB RAM |
| Приложение | Docker Desktop — 1 контейнер `smart-links:latest` |
| JVM | Temurin 25, Shenandoah GC, virtual threads |
| Redis | `redis:8.2-alpine` — отдельный контейнер |
| Gatling | 3.13.5, Java 25 (toolchain auto-provisioned) |
| Модель инъекции | Closed — persistent users, keep-alive соединения |

Закрытая модель (closed injection) была выбрана специально, чтобы не исчерпывать пул эфемерных портов Windows при высоком параллелизме.

### SmokeSim — дымовой тест

5 пользователей GET, 90 секунд (20 с разгон → 50 с нагрузка).

| Метрика | Значение |
|---|---|
| Всего запросов | 77 775 |
| Ошибок | 0 |
| p50 | 4 мс |
| p75 | 5 мс |
| p95 | 8 мс |
| p99 | 14 мс |
| Пропускная способность | 864 req/s |

### LoadSim — нагрузочный тест

40 пользователей GET + 10 пользователей POST, 10 минут (2 мин разгон → 5 мин нагрузка → 1 мин сброс).

| Запрос | Кол-во | p50 | p75 | p95 | p99 | req/s |
|---|---|---|---|---|---|---|
| `GET /s/{linkId}` | 789 565 | 20 мс | 28 мс | 46 мс | 79 мс | 1 316 |
| `POST /api/smartlinks` | 224 329 | 19 мс | 27 мс | 44 мс | 68 мс | 374 |
| **Итого** | **1 013 944** | **20 мс** | **27 мс** | **51 мс** | **189 мс** | **1 690** |

Ошибок: 0. Все assertion пройдены (GET p95 < 100 мс, GET p99 < 300 мс, POST p95 < 200 мс).

### StressSim — стресс-тест

Ступенчатый рост только GET: 10 → 25 → 50 → 75 → 100 → 150 → 200 пользователей (по 90 с на ступень).

| Метрика | Значение |
|---|---|
| Всего запросов | 1 467 574 |
| Ошибок | 0 |
| p50 | 29 мс |
| p75 | 50 мс |
| p95 | 104 мс |
| p99 | 881 мс |
| Пропускная способность | 1 882 req/s |

p99 выходит за 800 мс на уровне ≥ 150 пользователей — Redis throughput saturation под максимальной нагрузкой. До 100 пользователей p99 остаётся ниже 300 мс. Ошибок нет даже при 200 пользователях.

### SpikeSim — спайк-тест

10 пользователей (60 с) → рост до 100 за 2 с → удержание 120 с → возврат к 10 за 2 с → удержание 90 с.

| Метрика | Значение |
|---|---|
| Всего запросов | 817 517 |
| Ошибок | 0 |
| p50 | 22 мс |
| p75 | 30 мс |
| p95 | 53 мс |
| p99 | 223 мс |
| Пропускная способность | 2 271 req/s |

Система поглотила 10-кратный всплеск без единой ошибки. Assertion пройдены: success rate > 99 %, p99 < 1 000 мс.

### BreakSim — поиск потолка

Ступенчатый рост 500 → 1 000 → 2 000 → 3 000 → 4 000 → 5 000 пользователей (по 90 с на ступень).

Предыдущий результат (до virtual threads): ≥ 4 500 пользователей → 0,14 % HTTP 5xx (исчерпание пула потоков Tomcat). С virtual threads ограничение пула потоков снято — новый потолок определяется пропускной способностью Redis и сети.

### Сравнение до / после

| Метрика | До | После | Δ |
|---|---|---|---|
| SmokeSim throughput | 366 req/s | **864 req/s** | **+136 %** |
| LoadSim throughput | 1 137 req/s | **1 690 req/s** | **+49 %** |
| LoadSim GET p95 | 80 мс | **46 мс** | **−42 %** |
| LoadSim GET p99 | 169 мс | **79 мс** | **−53 %** |
| LoadSim POST p95 | 75 мс | **44 мс** | **−41 %** |
| StressSim throughput | 1 722 req/s | **1 882 req/s** | **+9 %** |
| StressSim p99 (200 польз.) | 1 115 мс | **881 мс** | **−21 %** |
| SpikeSim throughput | 2 052 req/s | **2 271 req/s** | **+11 %** |

### Выводы

**Virtual threads** — главная оптимизация. Исключает блокировку OS-потоков на I/O: 49 % роста throughput и 53 % снижения GET p99 в LoadSim. Tomcat 11 с Loom не уступает реактивным серверам в задачах Redis-lookup.

**Shenandoah GC** — снижение хвостовой latency. p99 в StressSim улучшился с 1 115 мс до 881 мс (−21 %) за счёт конкурентного сбора мусора без остановки приложения.

**Redis JSON serialization** — снижение CPU и payload. JDK-сериализация заменена на `StringRedisTemplate` + `JsonMapper`, индивидуальные ключи `sl:<id>` вместо единого хэша. Совместно с Lettuce pool (16 соединений) устраняет сериализацию запросов через одно соединение при высоком параллелизме.

**SpikeSim p99** вырос с 125 мс до 223 мс при сохранении 0 ошибок. Причина — побочный эффект оптимизации: более быстрые запросы повышают частоту цикла виртуальных пользователей, что увеличивает нагрузку на Redis во время пика (10×). Throughput при этом вырос на 11 %.

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
