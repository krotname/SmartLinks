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
- `ApiKeyAuthenticationFilter` closes the write surface behind a shared API key without touching public redirects.

## Security

Creating a Smart Link mints a URL on a trusted domain that redirects anywhere. An open write endpoint is therefore a ready-made stored open redirect and a bypass for any redirect allowlist, so writes are closed behind a shared API key.

| Property | Behaviour |
|---|---|
| Guarded | State-changing requests (`POST`, `PUT`, `PATCH`, `DELETE`) below `/api/**` |
| Public | `GET /s/{id}`, `/actuator/**`, Swagger UI — the redirect is the product and stays open |
| Header | `X-API-Key` |
| Key source | `smartlinks.api-key`, environment variable `SMARTLINKS_API_KEY` |
| Comparison | `MessageDigest.isEqual` — constant time, no timing oracle |
| Key not set | **Fail closed**: the application boots and keeps serving redirects, but every write answers 401 |
| Rejection | 401 plus `WWW-Authenticate`; a missing key and a wrong key are indistinguishable to the caller |

Matching happens on the path **within the application**: `server.servlet.context-path` is stripped before the `/api/` prefix is compared, otherwise a non-root context path (`/redirector/api/smartlinks`) would make the guard silently pass every write. That same path is normalised the way Spring MVC normalises it while routing (`;` path parameters, repeated slashes, percent-encoding), so the prefix cannot be dodged.

```bash
# Generate and export a key
export SMARTLINKS_API_KEY="$(openssl rand -base64 32)"

# Create a Smart Link
curl -X POST http://localhost:8080/api/smartlinks \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $SMARTLINKS_API_KEY" \
  -d '{"id":"demo","rules":[{"predicates":[],"args":{},"redirectTo":"https://otus.ru/default"}]}'

# Redirect — no key needed
curl -i http://localhost:8080/s/demo
```

On Kubernetes the key comes from the `smart-links-api-key` Secret:

```bash
kubectl create secret generic smart-links-api-key --from-literal=api-key="$SMARTLINKS_API_KEY"
```

## Engineering Rules

- Java production code and tests contain no explicit branch operators for business selection.
- Rule branching is expressed through Command, Factory, Strategy, and Stream API composition.
- Dependencies are declared through explicit constructors with no Lombok.
- Removed unused libraries: HTTP client, cloud BOM, utility library, URL validator, and a dedicated Redis test adapter.
- Spring Boot dependency management is declared through an explicit Gradle platform.
- Secrets are not stored in `application.yml`; local Redis runs without a default password and the API key comes from `SMARTLINKS_API_KEY`.

## Tests

- Unit tests target single roles: predicates, services, command chain, resolver.
- Web slice tests use the current Boot 4 `spring-boot-starter-webmvc-test` package.
- Redis integration tests use the official `redis:8.2-alpine` image through `GenericContainer`.
- `ApiKeyAuthenticationFilterTest` covers rejection without a key, rejection with a wrong key, the public redirect, fail-closed behaviour on a blank key, and a non-root `context-path`.
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

Version 2 applies four targeted optimizations; version 3 adds an L1 cache:

| Change | Implementation |
|---|---|
| **Virtual threads (Project Loom)** | `spring.threads.virtual.enabled=true` — each HTTP request gets a lightweight JVM virtual thread; the Tomcat OS thread pool is no longer the bottleneck |
| **Shenandoah GC** | `-XX:+UseShenandoahGC` in the Docker ENTRYPOINT (Ubuntu JRE — glibc is required for Shenandoah) |
| **Redis JSON instead of JDK serialization** | `StringRedisTemplate` + `JsonMapper` (Jackson 3, `tools.jackson`) + individual `sl:<id>` keys instead of a single mega-hash |
| **Lettuce connection pool** | `spring.data.redis.lettuce.pool.enabled=true`, 16 connections — virtual threads no longer serialize through a single Lettuce connection |
| **Caffeine L1 cache** | `Cache<String, String>` stores immutable JSON instead of externally mutable objects; 10,000 entries, `expireAfterAccess=10m`. Creation uses atomic Redis `SETNX`, so concurrent requests cannot overwrite a write-once link |

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
| Total requests | 82,700 |
| Errors | 0 |
| p50 | 4 ms |
| p75 | 5 ms |
| p95 | 9 ms |
| p99 | 14 ms |
| Throughput | 919 req/s |

### LoadSim — Load Test

40 GET users + 10 POST users, 10 minutes (2 min ramp → 5 min steady → 1 min drain).

| Request | Count | p50 | p75 | p95 | p99 | req/s |
|---|---|---|---|---|---|---|
| `GET /s/{linkId}` | 725,341 | 19 ms | 27 ms | 42 ms | 66 ms | 1,209 |
| `POST /api/smartlinks` | 209,319 | 20 ms | 27 ms | 43 ms | 64 ms | 349 |
| **Total** | **934,710** | **19 ms** | **27 ms** | **47 ms** | **178 ms** | **1,558** |

Errors: 0. All assertions passed (GET p95 < 100 ms, GET p99 < 300 ms, POST p95 < 200 ms).

### StressSim — Stress Test

Step ramp, GET-only: 10 → 25 → 50 → 75 → 100 → 150 → 200 concurrent users (90 s per step).

| Metric | Value |
|---|---|
| Total requests | 1,528,697 |
| Errors | 0 |
| p50 | 24 ms |
| p75 | 36 ms |
| p95 | 60 ms |
| p99 | 174 ms |
| Throughput | 1,960 req/s |

Caffeine eliminates Redis saturation: at 200 concurrent users, GET requests are served from memory while Redis receives only writes from the POST pool. p99 = 174 ms vs 881 ms without the cache (−80 %). Zero errors at all levels.

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

### Version-by-Version Comparison

| Metric | v1 (baseline) | v2 (VT + GC + JSON) | v3 (+Caffeine) | v1→v3 |
|---|---|---|---|---|
| SmokeSim throughput | 366 req/s | 864 req/s | **919 req/s** | **+151 %** |
| LoadSim GET p95 | 80 ms | 46 ms | **42 ms** | **−47 %** |
| LoadSim GET p99 | 169 ms | 79 ms | **66 ms** | **−61 %** |
| StressSim p99 (200 users) | 1,115 ms | 881 ms | **174 ms** | **−84 %** |
| StressSim throughput | 1,722 req/s | 1,882 req/s | **1,960 req/s** | **+14 %** |
| SpikeSim throughput | 2,052 req/s | 2,271 req/s | — | +11 % |

### Conclusions

**Virtual threads** are the primary first-stage driver. Eliminating OS-thread blocking on I/O yields a 49 % throughput increase and 53 % lower GET p99 in LoadSim. Tomcat 11 with Project Loom matches reactive servers for Redis-lookup workloads.

**Shenandoah GC** reduces tail latency. p99 in StressSim improved from 1,115 ms to 881 ms (−21 %) because GC cycles are concurrent and do not stop application threads.

**Redis JSON serialization + Lettuce pool** cut CPU and wire payload. JDK serialization replaced by `StringRedisTemplate` + `JsonMapper`, individual `sl:<id>` keys instead of a single hash. Combined with the pool (16 connections), eliminates serialization through a single connection under high concurrency.

**Caffeine L1 cache** is the primary second-stage driver. SmartLinks are write-once, read-many: 50 hot keys fit in Caffeine and are populated after successful creation and on the first Redis miss. At 200 concurrent GET users, Redis saturation was the bottleneck; the cache removes it entirely — p99 drops from 881 ms to 174 ms (−80 %). In this Docker-local benchmark (Redis sibling container, ~1 ms RTT) the gain at moderate load is smaller (LoadSim GET p99: 79→66 ms, −17 %). In production, where Redis is a separate host (10–50 ms), the effect is proportionally larger.

**SpikeSim p99** increased from 125 ms to 223 ms while maintaining zero errors. The cause is a side effect of optimization: faster per-request processing raises the virtual-user cycle rate, concentrating more Redis operations into the 10× spike window. Throughput still rose 11 %.

### Running the Tests

```powershell
# Required: the scenarios POST to /api/smartlinks and expect 201
$env:SMARTLINKS_API_KEY = "<key>"

# Build the image and start the environment
docker build -t smart-links:latest .
docker compose -p smartlinks-lt -f docker-compose.loadtest.yml up -d

# Run the scenarios (the key is read from SMARTLINKS_API_KEY, or pass -DapiKey=...)
.\gradlew.bat gatlingRunSmokeSim
.\gradlew.bat gatlingRunLoadSim
.\gradlew.bat gatlingRunStressSim
.\gradlew.bat gatlingRunSpikeSim
.\gradlew.bat gatlingRunBreakSim
```

HTML reports are saved to `build/gatling-results/`.
