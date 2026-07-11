package name.krot.smartlinks.load;

import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public abstract class SmartLinksSimulation extends Simulation {

    protected static final String BASE_URL =
            System.getProperty("baseUrl", "http://localhost:8090");

    static final int LINK_POOL_SIZE = 50;
    private static final String LINK_POOL_PREFIX = "load-lnk-" +
            UUID.randomUUID().toString().replace("-", "");

    protected final HttpProtocolBuilder protocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .disableFollowRedirect();

    protected final FeederBuilder<Object> linkIdFeeder = listFeeder(
            IntStream.rangeClosed(1, LINK_POOL_SIZE)
                    .mapToObj(i -> Map.<String, Object>of("linkId", linkId(i)))
                    .collect(Collectors.toList())
    ).random();

    protected final FeederBuilder<Object> userAgentFeeder = listFeeder(List.of(
            Map.<String, Object>of("ua", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36"),
            Map.<String, Object>of("ua", "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1"),
            Map.<String, Object>of("ua", "Mozilla/5.0 (iPad; CPU OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1"),
            Map.<String, Object>of("ua", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36"),
            Map.<String, Object>of("ua", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36")
    )).random();

    protected final FeederBuilder<Object> langFeeder = listFeeder(List.of(
            Map.<String, Object>of("lang", "ru-RU,ru;q=0.9,en;q=0.8"),
            Map.<String, Object>of("lang", "en-US,en;q=0.9"),
            Map.<String, Object>of("lang", "de-DE,de;q=0.9,en;q=0.8"),
            Map.<String, Object>of("lang", "fr-FR,fr;q=0.9,en;q=0.8"),
            Map.<String, Object>of("lang", "zh-CN,zh;q=0.9,en;q=0.8")
    )).random();

    /**
     * Creates LINK_POOL_SIZE SmartLinks before the main test. Uses repeat() with
     * open injection (atOnceUsers(1)) so the single seed user creates all links.
     */
    protected final ScenarioBuilder seedLinks = scenario("Seed SmartLinks")
            .exec(repeat(LINK_POOL_SIZE, "i").on(
                    exec(session ->
                            session.set("seedId", linkId(session.getInt("i") + 1))
                    ).exec(http("POST seed link")
                            .post("/api/smartlinks")
                            .body(StringBody(session -> buildSeedJson(session.getString("seedId"))))
                            .check(status().is(201)))
            ));

    /**
     * Persistent redirect user: loops indefinitely, reuses the HTTP connection.
     * Suitable for closed-model injection (constantConcurrentUsers / rampConcurrentUsers).
     */
    protected final ScenarioBuilder redirectLoop = scenario("GET Redirect")
            .asLongAs(session -> true).on(
                    feed(linkIdFeeder)
                            .feed(userAgentFeeder)
                            .feed(langFeeder)
                            .exec(http("GET /s/{linkId}")
                                    .get("/s/#{linkId}")
                                    .header("Accept-Language", "#{lang}")
                                    .header("User-Agent", "#{ua}")
                                    .check(status().is(302)))
            );

    /**
     * Persistent create user: each iteration creates a new SmartLink with a
     * random ID, exercising the write path end-to-end.
     */
    protected final ScenarioBuilder createLoop = scenario("POST SmartLink")
            .asLongAs(session -> true).on(
                    exec(session ->
                            session.set("newId", "wlnk-" +
                                    UUID.randomUUID().toString().replace("-", "").substring(0, 12))
                    ).exec(http("POST /api/smartlinks")
                            .post("/api/smartlinks")
                            .body(StringBody(session ->
                                    """
                                    {"id":"%s","rules":[{"predicates":[],"args":{},"redirectTo":"https://example.com/fallback"}]}
                                    """.formatted(session.getString("newId"))))
                            .check(status().is(201)))
            );

    private static String linkId(int index) {
        return "%s-%03d".formatted(LINK_POOL_PREFIX, index);
    }

    private static String buildSeedJson(String id) {
        return """
                {"id":"%s","rules":[
                  {"predicates":["Language"],"args":{"language":["ru","ru-RU"]},"redirectTo":"https://example.com/ru"},
                  {"predicates":["Language"],"args":{"language":["en","en-US"]},"redirectTo":"https://example.com/en"},
                  {"predicates":[],"args":{},"redirectTo":"https://example.com/default"}
                ]}""".formatted(id);
    }
}
