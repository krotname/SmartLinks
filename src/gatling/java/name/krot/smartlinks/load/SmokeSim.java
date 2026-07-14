package name.krot.smartlinks.load;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;

/**
 * Smoke test: sanity check at minimal load (5 concurrent users, ~70 s).
 *
 * Uses the closed injection model so behaviour stays consistent with the
 * other simulations. The seed scenario completes before redirect traffic starts.
 *
 * Assertions: zero errors, p95 < 500 ms.
 *
 * Run: ./gradlew gatlingRunSmokeSim
 */
public class SmokeSim extends SmartLinksSimulation {

    {
        setUp(
                seedLinks.injectOpen(atOnceUsers(1)).andThen(
                        redirectLoop.injectClosed(
                                rampConcurrentUsers(0).to(5).during(Duration.ofSeconds(20)),
                                constantConcurrentUsers(5).during(Duration.ofSeconds(50))
                        )
                )
        )
                .protocols(protocol)
                .maxDuration(Duration.ofSeconds(90))
                .assertions(
                        global().failedRequests().count().is(0L),
                        global().responseTime().percentile(95).lt(500)
                );
    }
}
