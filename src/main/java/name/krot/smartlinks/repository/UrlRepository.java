package name.krot.smartlinks.repository;

import lombok.RequiredArgsConstructor;
import name.krot.smartlinks.model.Url;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UrlRepository {

    private static final String KEY = "Url";

    private final RedisTemplate<String, Object> redisTemplate;

    public void save(Url url) {
        redisTemplate.opsForHash().put(KEY, url.getId(), url);
    }

    public Url findById(String id) {
        return (Url) redisTemplate.opsForHash().get(KEY, id);
    }
}