package name.krot.smartlinks.repository;

import name.krot.smartlinks.model.SmartLink;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class RedisSmartLinkRepository implements SmartLinkRepository {

    private static final String KEY = "SmartLink";

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisSmartLinkRepository(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(SmartLink smartLink) {
        redisTemplate.opsForHash().put(KEY, smartLink.getId(), smartLink);
    }

    @Override
    public Optional<SmartLink> findById(String id) {
        return Optional.ofNullable((SmartLink) redisTemplate.opsForHash().get(KEY, id));
    }
}
