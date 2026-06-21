package name.krot.smartlinks.config;

import org.springframework.context.annotation.Configuration;

// StringRedisTemplate and RedisTemplate are auto-configured by Spring Boot.
// Lettuce connection pool is activated by commons-pool2 on the classpath
// together with spring.data.redis.lettuce.pool.enabled=true in application.yml.
@Configuration
public class RedisConfig {
}
