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
