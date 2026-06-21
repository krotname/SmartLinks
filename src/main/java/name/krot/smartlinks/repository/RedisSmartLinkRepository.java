package name.krot.smartlinks.repository;

import name.krot.smartlinks.model.SmartLink;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;

@Repository
public class RedisSmartLinkRepository implements SmartLinkRepository {

    private static final String KEY_PREFIX = "sl:";

    private final StringRedisTemplate stringTemplate;
    private final JsonMapper jsonMapper;

    public RedisSmartLinkRepository(StringRedisTemplate stringTemplate, JsonMapper jsonMapper) {
        this.stringTemplate = stringTemplate;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void save(SmartLink smartLink) {
        try {
            stringTemplate.opsForValue().set(KEY_PREFIX + smartLink.getId(),
                    jsonMapper.writeValueAsString(smartLink));
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Cannot serialize SmartLink: " + smartLink.getId(), e);
        }
    }

    @Override
    public Optional<SmartLink> findById(String id) {
        String json = stringTemplate.opsForValue().get(KEY_PREFIX + id);
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(jsonMapper.readValue(json, SmartLink.class));
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Cannot deserialize SmartLink for key: " + id, e);
        }
    }
}
