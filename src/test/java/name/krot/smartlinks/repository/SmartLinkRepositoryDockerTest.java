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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
@DataRedisTest
@Import(SmartLinkRepositoryDockerTest.TestConfig.class)
class SmartLinkRepositoryDockerTest {

    @Container
    static RedisContainer redisContainer = new RedisContainer("redis:7.0.5-alpine");

    @Autowired
    private SmartLinkRepository smartLinkRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redisContainer::getHost);
        registry.add("spring.redis.port", redisContainer::getFirstMappedPort);
        registry.add("spring.redis.password", () -> "password");
    }

    @Test
    void testSaveAndFindById() {
        SmartLink smartLink = new SmartLink();
        smartLink.setId("smartlink123");

        smartLinkRepository.save(smartLink);

        SmartLink result = smartLinkRepository.findById("smartlink123");

        assertNotNull(result);
        assertEquals("smartlink123", result.getId());
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public SmartLinkRepository smartLinkRepository(RedisTemplate<String, Object> redisTemplate) {
            return new SmartLinkRepository(redisTemplate);
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
