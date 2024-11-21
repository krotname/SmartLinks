package name.krot.smartlinks.service;

import lombok.RequiredArgsConstructor;
import name.krot.smartlinks.model.Url;
import name.krot.smartlinks.repository.UrlRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UrlService {

    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int ID_LENGTH = 8;
    private SecureRandom random = new SecureRandom();

    private final UrlRepository urlRepository;

    public String shortenUrl(String longUrl) {
        String id = generateShortId();
        Url url = new Url(id, longUrl, System.currentTimeMillis(), System.currentTimeMillis(), 0);
        urlRepository.save(url);
        return id;
    }

    public Url getLongUrl(String id) {
        Url url = urlRepository.findById(id);
        if (url != null) {
            incrementAccessCount(url);
        }
        return url;
    }

    public void incrementAccessCount(Url url) {
        url.setAccessCount(url.getAccessCount() + 1);
        url.setLastAccessTime(System.currentTimeMillis());
        urlRepository.save(url);
    }

    private String generateShortId() {
        StringBuilder sb = new StringBuilder(ID_LENGTH);
        for (int i = 0; i < ID_LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    public Url getUrlDetails(String id) {
        return urlRepository.findById(id);
    }

}