package name.krot.smartlinks.repository;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import name.krot.smartlinks.model.SmartLink;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.Optional;

@Repository
public class RedisSmartLinkRepository implements SmartLinkRepository {

    private static final String KEY_PREFIX = "sl:";

    private final StringRedisTemplate stringTemplate;
    private final JsonMapper jsonMapper;
    private final Cache<String, String> localCache;

    public RedisSmartLinkRepository(StringRedisTemplate stringTemplate, JsonMapper jsonMapper) {
        this.stringTemplate = stringTemplate;
        this.jsonMapper = jsonMapper;
        this.localCache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterAccess(Duration.ofMinutes(10))
                .build();
    }

    @Override
    public boolean saveIfAbsent(SmartLink smartLink) {
        try {
            String json = jsonMapper.writeValueAsString(smartLink);
            boolean inserted = Boolean.TRUE.equals(stringTemplate.opsForValue()
                    .setIfAbsent(KEY_PREFIX + smartLink.getId(), json));
            if (inserted) {
                localCache.put(smartLink.getId(), json);
            }
            return inserted;
        } catch (JacksonException e) {
            throw new IllegalStateException("Cannot serialize SmartLink: " + smartLink.getId(), e);
        }
    }

    @Override
    public Optional<SmartLink> findById(String id) {
        String cached = localCache.getIfPresent(id);
        if (cached != null) {
            return Optional.of(deserialize(id, cached));
        }
        String json = stringTemplate.opsForValue().get(KEY_PREFIX + id);
        if (json == null) {
            return Optional.empty();
        }
        SmartLink smartLink = deserialize(id, json);
        localCache.put(id, json);
        return Optional.of(smartLink);
    }

    private SmartLink deserialize(String id, String json) {
        try {
            // Redis is a durable cross-version boundary. Ignore fields written by a newer
            // application version so rolling deployments do not break older replicas.
            SmartLink smartLink = jsonMapper.readerFor(SmartLink.class)
                    .without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .readValue(json);
            if (!id.equals(smartLink.getId())) {
                throw new IllegalStateException("Stored SmartLink id does not match Redis key: " + id);
            }
            return smartLink;
        } catch (JacksonException e) {
            throw new IllegalStateException("Cannot deserialize SmartLink for key: " + id, e);
        }
    }
}
