package name.krot.smartlinks.load;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;

/**
 * Load test: sustained production-like traffic using the closed injection model.
 *
 * Closed model — a fixed pool of persistent virtual users each loop continuously,
 * reusing TCP connections (HTTP keep-alive).  This mirrors real connection pools
 * and avoids ephemeral-port exhaustion on developer machines.
 *
 * Profile (8 min total, 80 % reads / 20 % writes):
 *   Redirect users: 0 → 40  over 2 min, hold 5 min, drain 1 min
 *   Create  users: 0 → 10  over 2 min, hold 5 min, drain 1 min
 *
 * Seed (atOnceUsers 1) runs in parallel; completes in ~1 s — well before
 * the 2-min ramp reaches meaningful load.
 *
 * Assertions: p95 < 100 ms (GET), p99 < 300 ms (GET), error rate < 1 %.
 *
 * Run: ./gradlew gatlingRunLoadSim
 */
public class LoadSim extends SmartLinksSimulation {

    {
        setUp(
                seedLinks.injectOpen(atOnceUsers(1)),

                redirectLoop.injectClosed(
                        rampConcurrentUsers(0).to(40).during(Duration.ofMinutes(2)),
                        constantConcurrentUsers(40).during(Duration.ofMinutes(5)),
                        rampConcurrentUsers(40).to(0).during(Duration.ofMinutes(1))
                ),

                createLoop.injectClosed(
                        rampConcurrentUsers(0).to(10).during(Duration.ofMinutes(2)),
                        constantConcurrentUsers(10).during(Duration.ofMinutes(5)),
                        rampConcurrentUsers(10).to(0).during(Duration.ofMinutes(1))
                )
        )
                .protocols(protocol)
                .maxDuration(Duration.ofMinutes(10))
                .assertions(
                        global().failedRequests().percent().lt(1.0),
                        details("GET /s/{linkId}").responseTime().percentile(95).lt(100),
                        details("GET /s/{linkId}").responseTime().percentile(99).lt(300),
                        details("POST /api/smartlinks").responseTime().percentile(95).lt(200)
                );
    }
}
