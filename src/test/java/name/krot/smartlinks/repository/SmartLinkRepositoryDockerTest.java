package name.krot.smartlinks.repository;

import com.redis.testcontainers.RedisContainer;
import name.krot.smartlinks.model.SmartLink;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static name.krot.smartlinks.support.SmartLinksTestFixtures.SMART_LINK_ID;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.smartLink;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
@DataRedisTest
@Import(SmartLinkRepositoryDockerTest.TestConfig.class)
class SmartLinkRepositoryDockerTest {

    private static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:7.0.5-alpine");

    @Container
    static RedisContainer redisContainer = new RedisContainer(REDIS_IMAGE);

    @Autowired
    private SmartLinkRepository smartLinkRepository;

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort);
        registry.add("spring.data.redis.password", () -> "password");
    }

    @Test
    void testSaveAndFindById() {
        SmartLink smartLink = smartLink();

        smartLinkRepository.save(smartLink);

        SmartLink result = smartLinkRepository.findById(SMART_LINK_ID).orElse(null);

        assertNotNull(result);
        assertEquals(SMART_LINK_ID, result.getId());
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public SmartLinkRepository smartLinkRepository(RedisTemplate<String, Object> redisTemplate) {
            return new RedisSmartLinkRepository(redisTemplate);
        }

        @Bean
        public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
            RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(connectionFactory);
            return template;
        }

        @Bean
        public RedisConnectionFactory redisConnectionFactory() {
            LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory();
            lettuceConnectionFactory.setPort(redisContainer.getFirstMappedPort());
            return lettuceConnectionFactory;
        }
    }
}
