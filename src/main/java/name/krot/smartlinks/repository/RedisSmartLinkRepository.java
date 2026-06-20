package name.krot.smartlinks.repository;

import lombok.RequiredArgsConstructor;
import name.krot.smartlinks.model.SmartLink;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RedisSmartLinkRepository implements SmartLinkRepository {

    private static final String KEY = "SmartLink";

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void save(SmartLink smartLink) {
        redisTemplate.opsForHash().put(KEY, smartLink.getId(), smartLink);
    }

    @Override
    public Optional<SmartLink> findById(String id) {
        return Optional.ofNullable((SmartLink) redisTemplate.opsForHash().get(KEY, id));
    }
}
