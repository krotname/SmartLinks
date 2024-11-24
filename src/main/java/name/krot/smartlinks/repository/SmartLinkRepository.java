package name.krot.smartlinks.repository;

import lombok.RequiredArgsConstructor;
import name.krot.smartlinks.model.SmartLink;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SmartLinkRepository {

    private static final String KEY = "SmartLink";

    private final RedisTemplate<String, Object> redisTemplate;

    public void save(SmartLink smartLink) {
        redisTemplate.opsForHash().put(KEY, smartLink.getId(), smartLink);
    }

    public SmartLink findById(String id) {
        return (SmartLink) redisTemplate.opsForHash().get(KEY, id);
    }
}

