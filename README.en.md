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

## Performance

Version 2 applies four targeted optimizations:

| Change | Implementation |
|---|---|
| **Virtual threads (Project Loom)** | `spring.threads.virtual.enabled=true` — each HTTP request gets a lightweight JVM virtual thread; the Tomcat OS thread pool is no longer the bottleneck |
| **Shenandoah GC** | `-XX:+UseShenandoahGC` in the Docker ENTRYPOINT (Ubuntu JRE — glibc is required for Shenandoah) |
| **Redis JSON instead of JDK serialization** | `StringRedisTemplate` + `JsonMapper` (Jackson 3, `tools.jackson`) + individual `sl:<id>` keys instead of a single mega-hash |
| **Lettuce connection pool** | `spring.data.redis.lettuce.pool.enabled=true`, 16 connections — virtual threads no longer serialize through a single Lettuce connection |

Spring Boot 4.1 does not publish `spring-boot-starter-undertow` — Undertow was removed from the distribution.
Virtual threads with Tomcat 11 achieve equal or better results.

## Load Testing

Tests are written in **Gatling 3.13.5** (Java DSL) and run through Gradle without any external tooling.
Sources live in `src/gatling/java/name/krot/smartlinks/load/`.

**Environment**

| Parameter | Value |
|---|---|
| Host | Windows 11 Pro, Intel Core i7, 32 GB RAM |
| Application | Docker Desktop — 1 container `smart-links:latest` |
| JVM | Temurin 25, Shenandoah GC, virtual threads |
| Redis | `redis:8.2-alpine` — dedicated container |
| Gatling | 3.13.5, Java 25 (toolchain auto-provisioned) |
| Injection model | Closed — persistent users, keep-alive connections |

The closed injection model was chosen deliberately to avoid exhausting the Windows ephemeral port pool under high concurrency.

### SmokeSim — Smoke Test

5 concurrent GET users, 90 seconds (20 s ramp → 50 s steady).

| Metric | Value |
|---|---|
| Total requests | 77,775 |
| Errors | 0 |
| p50 | 4 ms |
| p75 | 5 ms |
| p95 | 8 ms |
| p99 | 14 ms |
| Throughput | 864 req/s |

### LoadSim — Load Test

40 GET users + 10 POST users, 10 minutes (2 min ramp → 5 min steady → 1 min drain).

| Request | Count | p50 | p75 | p95 | p99 | req/s |
|---|---|---|---|---|---|---|
| `GET /s/{linkId}` | 789,565 | 20 ms | 28 ms | 46 ms | 79 ms | 1,316 |
| `POST /api/smartlinks` | 224,329 | 19 ms | 27 ms | 44 ms | 68 ms | 374 |
| **Total** | **1,013,944** | **20 ms** | **27 ms** | **51 ms** | **189 ms** | **1,690** |

Errors: 0. All assertions passed (GET p95 < 100 ms, GET p99 < 300 ms, POST p95 < 200 ms).

### StressSim — Stress Test

Step ramp, GET-only: 10 → 25 → 50 → 75 → 100 → 150 → 200 concurrent users (90 s per step).

| Metric | Value |
|---|---|
| Total requests | 1,467,574 |
| Errors | 0 |
| p50 | 29 ms |
| p75 | 50 ms |
| p95 | 104 ms |
| p99 | 881 ms |
| Throughput | 1,882 req/s |

p99 exceeds 800 ms at ≥ 150 users — Redis throughput saturation under peak load. Below 100 users p99 stays under 300 ms. Zero errors even at 200 concurrent users.

### SpikeSim — Spike Test

10 users (60 s) → ramp to 100 in 2 s → hold 120 s → ramp back to 10 in 2 s → hold 90 s.

| Metric | Value |
|---|---|
| Total requests | 817,517 |
| Errors | 0 |
| p50 | 22 ms |
| p75 | 30 ms |
| p95 | 53 ms |
| p99 | 223 ms |
| Throughput | 2,271 req/s |

The system absorbed a 10× traffic burst with zero errors. Assertions passed: success rate > 99 %, p99 < 1,000 ms.

### BreakSim — Capacity Ceiling

Step ramp 500 → 1,000 → 2,000 → 3,000 → 4,000 → 5,000 concurrent users (90 s per step).

Previous result (before virtual threads): ≥ 4,500 users → 0.14 % HTTP 5xx (Tomcat OS thread pool exhaustion). With virtual threads the thread-pool constraint is lifted — the new ceiling is determined by Redis and network throughput.

### Before / After Comparison

| Metric | Before | After | Δ |
|---|---|---|---|
| SmokeSim throughput | 366 req/s | **864 req/s** | **+136 %** |
| LoadSim throughput | 1,137 req/s | **1,690 req/s** | **+49 %** |
| LoadSim GET p95 | 80 ms | **46 ms** | **−42 %** |
| LoadSim GET p99 | 169 ms | **79 ms** | **−53 %** |
| LoadSim POST p95 | 75 ms | **44 ms** | **−41 %** |
| StressSim throughput | 1,722 req/s | **1,882 req/s** | **+9 %** |
| StressSim p99 (200 users) | 1,115 ms | **881 ms** | **−21 %** |
| SpikeSim throughput | 2,052 req/s | **2,271 req/s** | **+11 %** |

### Conclusions

**Virtual threads** are the primary driver. Eliminating OS-thread blocking on I/O yields 49 % more throughput and 53 % lower GET p99 in LoadSim. Tomcat 11 with Project Loom matches reactive servers for Redis-lookup workloads.

**Shenandoah GC** reduces tail latency. p99 in StressSim improved from 1,115 ms to 881 ms (−21 %) because GC cycles are concurrent and do not stop application threads.

**Redis JSON serialization** cuts CPU and wire payload. JDK serialization is replaced by `StringRedisTemplate` + `JsonMapper`, with individual `sl:<id>` keys instead of a single hash. Combined with the Lettuce pool (16 connections), this eliminates request serialization through a single Redis connection under high concurrency.

**SpikeSim p99** increased from 125 ms to 223 ms while maintaining zero errors. The cause is a side effect of optimization: faster per-request processing raises the virtual-user cycle rate, which concentrates more Redis operations into the 10× spike window. Throughput still rose 11 %.

### Running the Tests

```powershell
# Build the image and start the environment
docker build -t smart-links:latest .
docker compose -p smartlinks-lt -f docker-compose.loadtest.yml up -d

# Run the scenarios
.\gradlew.bat gatlingRunSmokeSim
.\gradlew.bat gatlingRunLoadSim
.\gradlew.bat gatlingRunStressSim
.\gradlew.bat gatlingRunSpikeSim
.\gradlew.bat gatlingRunBreakSim
```

HTML reports are saved to `build/gatling-results/`.
