package name.krot.smartlinks.load;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;

/**
 * Spike test: sudden 10× traffic burst followed by a recovery period.
 *
 * Uses the closed injection model so that connection state is preserved and
 * the test reflects application-level recovery rather than OS networking warm-up.
 *
 * Profile (redirect-only):
 *   Phase 1 – baseline  : 10 concurrent users for 60 s
 *   Phase 2 – spike     : ramp to 100 users over 2 s, hold 120 s
 *   Phase 3 – recovery  : ramp back to 10 users over 2 s, hold 90 s
 *
 * Assertions: overall success rate ≥ 99 %, p99 < 1 000 ms.
 *
 * Run: ./gradlew gatlingRunSpikeSim
 */
public class SpikeSim extends SmartLinksSimulation {

    {
        setUp(
                seedLinks.injectOpen(atOnceUsers(1)).andThen(
                        redirectLoop.injectClosed(
                                // baseline
                                rampConcurrentUsers(0).to(10).during(Duration.ofSeconds(2)),
                                constantConcurrentUsers(10).during(Duration.ofSeconds(60)),

                                // spike — near-instantaneous ramp (10×)
                                rampConcurrentUsers(10).to(100).during(Duration.ofSeconds(2)),
                                constantConcurrentUsers(100).during(Duration.ofSeconds(120)),

                                // recovery
                                rampConcurrentUsers(100).to(10).during(Duration.ofSeconds(2)),
                                constantConcurrentUsers(10).during(Duration.ofSeconds(90))
                        )
                )
        )
                .protocols(protocol)
                .maxDuration(Duration.ofMinutes(6))
                .assertions(
                        global().successfulRequests().percent().gt(99.0),
                        global().responseTime().percentile(99).lt(1000)
                );
    }
}
