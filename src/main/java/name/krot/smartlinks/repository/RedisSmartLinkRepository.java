package name.krot.smartlinks.repository;

import name.krot.smartlinks.model.SmartLink;
import name.krot.smartlinks.model.VersionedSmartLink;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RedisSmartLinkRepository implements SmartLinkRepository, ConditionalSmartLinkRepository {

    private static final String KEY_PREFIX = "sl:";

    private final StringRedisTemplate stringTemplate;
    private final JsonMapper jsonMapper;
    private static final DefaultRedisScript<Long> REPLACE = new DefaultRedisScript<>("""
            local current = redis.call('GET', KEYS[1])
            if not current then return 0 end
            local version = cjson.decode(current)._revision or '0'
            if version ~= ARGV[1] then return 0 end
            redis.call('SET', KEYS[1], ARGV[2], 'KEEPTTL')
            return 1
            """, Long.class);

    public RedisSmartLinkRepository(StringRedisTemplate stringTemplate, JsonMapper jsonMapper) {
        this.stringTemplate = stringTemplate;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public boolean saveIfAbsent(SmartLink smartLink) {
        try {
            String json = jsonMapper.writeValueAsString(smartLink);
            boolean inserted = Boolean.TRUE.equals(stringTemplate.opsForValue()
                    .setIfAbsent(KEY_PREFIX + smartLink.getId(), json));
            return inserted;
        } catch (JacksonException e) {
            throw new IllegalStateException("Cannot serialize SmartLink: " + smartLink.getId(), e);
        }
    }

    @Override
    public Optional<SmartLink> findById(String id) {
        String json = stringTemplate.opsForValue().get(KEY_PREFIX + id);
        if (json == null) {
            return Optional.empty();
        }
        SmartLink smartLink = deserialize(id, json);
        return Optional.of(smartLink);
    }

    @Override
    public Optional<VersionedSmartLink> findVersionedById(String id) {
        String json = stringTemplate.opsForValue().get(KEY_PREFIX + id);
        if (json == null) return Optional.empty();
        SmartLink definition = deserialize(id, json);
        String version = jsonMapper.readTree(json).path("_revision").asString("0");
        return Optional.of(new VersionedSmartLink(definition, version));
    }

    @Override
    public Optional<VersionedSmartLink> replaceIfVersionMatches(SmartLink replacement, String expectedVersion) {
        String version = UUID.randomUUID().toString();
        var document = jsonMapper.valueToTree(replacement);
        ((tools.jackson.databind.node.ObjectNode) document).put("_revision", version);
        Long updated = stringTemplate.execute(REPLACE, List.of(KEY_PREFIX + replacement.getId()),
                expectedVersion, jsonMapper.writeValueAsString(document));
        return Long.valueOf(1).equals(updated)
                ? Optional.of(new VersionedSmartLink(replacement, version)) : Optional.empty();
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
