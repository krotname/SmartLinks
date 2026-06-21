package name.krot.smartlinks.repository;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import name.krot.smartlinks.model.SmartLink;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.Optional;

@Repository
public class RedisSmartLinkRepository implements SmartLinkRepository {

    private static final String KEY_PREFIX = "sl:";

    private final StringRedisTemplate stringTemplate;
    private final JsonMapper jsonMapper;
    private final Cache<String, SmartLink> localCache;

    public RedisSmartLinkRepository(StringRedisTemplate stringTemplate, JsonMapper jsonMapper) {
        this.stringTemplate = stringTemplate;
        this.jsonMapper = jsonMapper;
        this.localCache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterAccess(Duration.ofMinutes(10))
                .build();
    }

    @Override
    public void save(SmartLink smartLink) {
        try {
            stringTemplate.opsForValue().set(KEY_PREFIX + smartLink.getId(),
                    jsonMapper.writeValueAsString(smartLink));
            localCache.put(smartLink.getId(), smartLink);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Cannot serialize SmartLink: " + smartLink.getId(), e);
        }
    }

    @Override
    public Optional<SmartLink> findById(String id) {
        SmartLink cached = localCache.getIfPresent(id);
        if (cached != null) {
            return Optional.of(cached);
        }
        String json = stringTemplate.opsForValue().get(KEY_PREFIX + id);
        if (json == null) {
            return Optional.empty();
        }
        try {
            SmartLink smartLink = jsonMapper.readValue(json, SmartLink.class);
            localCache.put(id, smartLink);
            return Optional.of(smartLink);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Cannot deserialize SmartLink for key: " + id, e);
        }
    }
}
