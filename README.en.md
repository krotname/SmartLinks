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

## Load Testing

Tests are written in **Gatling 3.13.5** (Java DSL) and run through Gradle without any external tooling.
Sources live in `src/gatling/java/name/krot/smartlinks/load/`.

**Environment**

| Parameter | Value |
|---|---|
| Host | Windows 11 Pro, Intel Core i7, 32 GB RAM |
| Application | Docker Desktop — 1 container `smart-links:latest` |
| Redis | `redis:8.2-alpine` — dedicated container |
| Gatling | 3.13.5, Java 25 (toolchain auto-provisioned) |
| Injection model | Closed — persistent users, keep-alive connections |

The closed injection model was chosen deliberately to avoid exhausting the Windows ephemeral port pool under high concurrency.

### SmokeSim — Smoke Test

5 concurrent GET users, 90 seconds (20 s ramp → 50 s steady).

| Metric | Value |
|---|---|
| Total requests | 32,909 |
| Errors | 0 |
| p50 | 8 ms |
| p75 | 12 ms |
| p95 | 28 ms |
| p99 | 61 ms |
| Throughput | 366 req/s |

### LoadSim — Load Test

40 GET users + 10 POST users, 10 minutes (2 min ramp → 5 min steady → 1 min drain).

| Request | Count | p50 | p75 | p95 | p99 | req/s |
|---|---|---|---|---|---|---|
| `GET /s/{linkId}` | 532,086 | 29 ms | 43 ms | 80 ms | 169 ms | 887 |
| `POST /api/smartlinks` | 149,896 | 28 ms | 40 ms | 75 ms | 142 ms | 250 |
| **Total** | **682,032** | **29 ms** | **42 ms** | **78 ms** | **163 ms** | **1,137** |

Errors: 0. All assertions passed (GET p95 < 100 ms, GET p99 < 300 ms, POST p95 < 200 ms).

### StressSim — Stress Test

Step ramp, GET-only: 10 → 25 → 50 → 75 → 100 → 150 → 200 concurrent users (90 s per step).

| Metric | Value |
|---|---|
| Total requests | 1,343,152 |
| Errors | 0 |
| p50 | 36 ms |
| p75 | 56 ms |
| p95 | 100 ms |
| p99 | 1,115 ms |
| Throughput | 1,722 req/s |

p99 exceeds 1 s at ≥ 150 users — Redis pipeline / JVM GC pressure. Below 100 users p99 stays under 300 ms. Zero errors even at 200 concurrent users.

### SpikeSim — Spike Test

10 users (60 s) → ramp to 100 in 2 s → hold 120 s → ramp back to 10 in 2 s → hold 90 s.

| Metric | Value |
|---|---|
| Total requests | 738,634 |
| Errors | 0 |
| p50 | 25 ms |
| p75 | 34 ms |
| p95 | 52 ms |
| p99 | 125 ms |
| Throughput | 2,052 req/s |

The system absorbed a 10× traffic burst with zero errors. Assertions passed: success rate > 99 %, p99 < 1,000 ms.

### BreakSim — Capacity Ceiling

Step ramp 500 → 1,000 → 2,000 → 3,000 → 4,000 → 5,000 concurrent users (90 s per step).

| Parameter | Value |
|---|---|
| Total requests | 690,009 |
| Errors | first appear at ≥ 4,500 users |
| Errors at 4,500 → 5,000 | 997 (0.14 %) |
| Error type | HTTP 5xx, mean response 3.4 s (thread pool saturation) |
| p50 (whole test) | 534 ms |
| p95 (whole test) | 1,189 ms |
| Throughput ceiling | ~2,100–2,300 req/s |

**Findings:**

| User range | Errors | p50 | p99 |
|---|---|---|---|
| ≤ 200 users | 0 | 36 ms | 1,115 ms |
| 200 – 1,500 | 0 | 233 ms | 899 ms |
| 1,500 – 4,000 | 0 | ~970 ms | ~3,700 ms |
| 4,500 – 5,000 | **0.14 %** | 534 ms | 2,225 ms |

The throughput ceiling is ~2,100 req/s. Above ~4,500 persistent connections the Tomcat thread pool saturates and requests start receiving 5xx responses. Below that threshold the accept queue absorbs all load without dropping a single request.

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
