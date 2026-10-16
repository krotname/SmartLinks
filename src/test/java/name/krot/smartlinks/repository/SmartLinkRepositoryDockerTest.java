package name.krot.smartlinks.repository;

import name.krot.smartlinks.model.SmartLink;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.redis.test.autoconfigure.DataRedisTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.json.JsonMapper;

import static name.krot.smartlinks.support.SmartLinksTestFixtures.SMART_LINK_ID;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.fallbackRule;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.smartLink;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
@DataRedisTest
@Import({RedisSmartLinkRepository.class, SmartLinkRepositoryDockerTest.TestConfig.class})
class SmartLinkRepositoryDockerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        JsonMapper jsonMapper() {
            return JsonMapper.builder().build();
        }
    }

    private static final DockerImageName REDIS_IMAGE = DockerImageName.parse(
            "redis:8.2-alpine@sha256:94589dc90eb8b7eb920f3a9a6d85657e73fd20db61410719500b9c5ba0548d9a"
    );

    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>(REDIS_IMAGE)
            .withExposedPorts(6379);

    @Autowired
    private SmartLinkRepository smartLinkRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void independentReplicasImmediatelyObserveEditsToLegacyDocuments() throws Exception {
        String id = "edit-legacy";
        SmartLink original = smartLink(fallbackRule("https://example.com/old"));
        original.setId(id);
        stringRedisTemplate.opsForValue().set("sl:" + id, jsonMapper.writeValueAsString(original));
        var reader = new RedisSmartLinkRepository(stringRedisTemplate, jsonMapper);
        var writer = new RedisSmartLinkRepository(stringRedisTemplate, jsonMapper);
        assertEquals("https://example.com/old", reader.findById(id).orElseThrow().getRules().getFirst().getRedirectTo());
        var before = writer.findVersionedById(id).orElseThrow();
        assertEquals("0", before.version());
        SmartLink replacement = smartLink(fallbackRule("https://example.com/new"));
        replacement.setId(id);
        var after = writer.replaceIfVersionMatches(replacement, before.version()).orElseThrow();
        assertFalse(after.version().equals(before.version()));
        assertEquals("https://example.com/new", reader.findById(id).orElseThrow().getRules().getFirst().getRedirectTo());
        assertTrue(writer.replaceIfVersionMatches(original, before.version()).isEmpty());
    }

    @Test
    void exactlyOneConcurrentWriterWins() throws Exception {
        String id = "edit-race";
        SmartLink original = smartLink(fallbackRule("https://example.com/original"));
        original.setId(id);
        assertTrue(smartLinkRepository.saveIfAbsent(original));
        var first = new RedisSmartLinkRepository(stringRedisTemplate, jsonMapper);
        var second = new RedisSmartLinkRepository(stringRedisTemplate, jsonMapper);
        String version = first.findVersionedById(id).orElseThrow().version();
        var start = new java.util.concurrent.CountDownLatch(1);
        try (var workers = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var a = workers.submit(() -> { start.await(); return first.replaceIfVersionMatches(original, version).isPresent(); });
            var b = workers.submit(() -> { start.await(); return second.replaceIfVersionMatches(original, version).isPresent(); });
            start.countDown();
            assertTrue(a.get(10, java.util.concurrent.TimeUnit.SECONDS) ^ b.get(10, java.util.concurrent.TimeUnit.SECONDS));
        }
    }

    @Test
    void conditionalWriteNeverCreatesMissingKeys() {
        var repository = new RedisSmartLinkRepository(stringRedisTemplate, jsonMapper);
        SmartLink missing = smartLink(fallbackRule("https://example.com/missing"));
        missing.setId("edit-missing");
        assertTrue(repository.replaceIfVersionMatches(missing, "0").isEmpty());
        assertTrue(repository.findById("edit-missing").isEmpty());
    }

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort);
    }

    @Test
    void createsSmartLinkOnceWithoutOverwritingRedis() {
        SmartLink smartLink = smartLink(fallbackRule("https://example.com/original"));
        SmartLink replacement = smartLink(fallbackRule("https://example.com/replacement"));

        assertTrue(smartLinkRepository.saveIfAbsent(smartLink));
        assertFalse(smartLinkRepository.saveIfAbsent(replacement));

        SmartLinkRepository uncachedRepository = new RedisSmartLinkRepository(stringRedisTemplate, jsonMapper);
        SmartLink result = uncachedRepository.findById(SMART_LINK_ID).orElseThrow();

        assertNotNull(result);
        assertEquals(SMART_LINK_ID, result.getId());
        assertEquals("https://example.com/original", result.getRules().getFirst().getRedirectTo());
    }
}
