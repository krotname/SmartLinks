package name.krot.smartlinks.load;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;

/**
 * Stress test: step ramp to find throughput ceiling (closed injection model).
 *
 * Each step adds more persistent concurrent users that loop continuously,
 * reusing TCP connections.  This isolates application/Redis latency from
 * OS networking limits.
 *
 * Profile — redirect-only (pure read), 7 steps × 90 s:
 *   10 → 25 → 50 → 75 → 100 → 150 → 200 concurrent users
 *
 * No pass/fail assertions: examine the HTML report to find where error rate
 * exceeds 1 % or p95 latency spikes above 1 000 ms.  That inflection point
 * is the system's throughput ceiling on this hardware.
 *
 * Run: ./gradlew gatlingRunStressSim
 */
public class StressSim extends SmartLinksSimulation {

    private static final Duration STEP = Duration.ofSeconds(90);

    {
        setUp(
                seedLinks.injectOpen(atOnceUsers(1)),

                redirectLoop.injectClosed(
                        rampConcurrentUsers(0).to(10).during(Duration.ofSeconds(5)),
                        constantConcurrentUsers(10).during(STEP),

                        rampConcurrentUsers(10).to(25).during(Duration.ofSeconds(5)),
                        constantConcurrentUsers(25).during(STEP),

                        rampConcurrentUsers(25).to(50).during(Duration.ofSeconds(5)),
                        constantConcurrentUsers(50).during(STEP),

                        rampConcurrentUsers(50).to(75).during(Duration.ofSeconds(5)),
                        constantConcurrentUsers(75).during(STEP),

                        rampConcurrentUsers(75).to(100).during(Duration.ofSeconds(5)),
                        constantConcurrentUsers(100).during(STEP),

                        rampConcurrentUsers(100).to(150).during(Duration.ofSeconds(5)),
                        constantConcurrentUsers(150).during(STEP),

                        rampConcurrentUsers(150).to(200).during(Duration.ofSeconds(5)),
                        constantConcurrentUsers(200).during(STEP)
                )
        )
                .protocols(protocol)
                .maxDuration(Duration.ofMinutes(13));
    }
}
