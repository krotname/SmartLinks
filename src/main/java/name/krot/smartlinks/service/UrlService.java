package name.krot.smartlinks.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import name.krot.smartlinks.model.Url;
import name.krot.smartlinks.repository.UrlRepository;
import org.springframework.stereotype.Service;
import org.apache.commons.validator.routines.UrlValidator;

import java.security.SecureRandom;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class UrlService {

    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int ID_LENGTH = 8;
    private SecureRandom random = new SecureRandom();

    private final UrlRepository urlRepository;

    public String shortenUrl(String longUrl) {
        log.info("Received URL to shorten: {}", longUrl);
        if (!isValidUrl(longUrl)) {
            throw new IllegalArgumentException("Invalid URL");
        }
        String id = generateShortId();
        Url url = new Url(id, longUrl, System.currentTimeMillis(), System.currentTimeMillis(), 0);
        urlRepository.save(url);
        return id;
    }

    public Url getLongUrl(String id) {
        log.info("Retrieving long URL for id: {}", id);
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

    private boolean isValidUrl(String url) {
        UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});
        return urlValidator.isValid(url);
    }

    private String generateShortId() {
        String id;
        do {
            StringBuilder sb = new StringBuilder(ID_LENGTH);
            for (int i = 0; i < ID_LENGTH; i++) {
                sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
            }
            id = sb.toString();
        } while (urlRepository.findById(id) != null);
        return id;
    }

    public Url getUrlDetails(String id) {
        return urlRepository.findById(id);
    }

}