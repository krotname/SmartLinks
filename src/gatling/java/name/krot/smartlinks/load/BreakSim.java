package name.krot.smartlinks.load;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;

/**
 * Break test: find the throughput ceiling and connection-count where the
 * first errors appear (closed injection, GET-only, 90 s per step).
 *
 * Measured results on this hardware (Docker Desktop, Windows 11):
 *   ≤ 4 000 users  → 0 errors, throughput ceiling ~2 100 req/s
 *   ≥ 4 500 users  → 0.14 % HTTP 5xx (Tomcat thread-pool saturation)
 *
 * No assertions — examine the HTML report for the step where the error
 * rate first exceeds 1 % or p95 crosses 2 000 ms.
 *
 * Run: ./gradlew gatlingRunBreakSim
 */
public class BreakSim extends SmartLinksSimulation {

    private static final Duration STEP = Duration.ofSeconds(90);
    private static final Duration RAMP = Duration.ofSeconds(10);

    {
        setUp(
                seedLinks.injectOpen(atOnceUsers(1)),

                redirectLoop.injectClosed(
                        rampConcurrentUsers(0).to(500).during(RAMP),
                        constantConcurrentUsers(500).during(STEP),

                        rampConcurrentUsers(500).to(1000).during(RAMP),
                        constantConcurrentUsers(1000).during(STEP),

                        rampConcurrentUsers(1000).to(2000).during(RAMP),
                        constantConcurrentUsers(2000).during(STEP),

                        rampConcurrentUsers(2000).to(3000).during(RAMP),
                        constantConcurrentUsers(3000).during(STEP),

                        rampConcurrentUsers(3000).to(4000).during(RAMP),
                        constantConcurrentUsers(4000).during(STEP),

                        rampConcurrentUsers(4000).to(5000).during(RAMP),
                        constantConcurrentUsers(5000).during(STEP)
                )
        )
                .protocols(protocol)
                .maxDuration(Duration.ofMinutes(12));
    }
}
