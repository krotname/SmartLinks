package name.krot.smartlinks.service;

import name.krot.smartlinks.model.Url;
import name.krot.smartlinks.repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Disabled
class UrlServiceTest {

    @InjectMocks
    private UrlService urlService;

    @Mock
    private UrlRepository urlRepository;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testShortenUrl_ValidUrl() {
        String longUrl = "https://www.example.com";
        when(urlRepository.findById(anyString())).thenReturn(null);

        String shortId = urlService.shortenUrl(longUrl);

        assertNotNull(shortId);
        verify(urlRepository, times(1)).save(any(Url.class));
    }

    @Test
    void testShortenUrl_InvalidUrl() {
        String longUrl = "invalid_url";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            urlService.shortenUrl(longUrl);
        });

        assertEquals("Invalid URL", exception.getMessage());
    }

    @Test
    void testGetLongUrl_Found() {
        String shortId = "abc123";
        Url url = new Url(shortId, "https://www.example.com", System.currentTimeMillis(), System.currentTimeMillis(), 0);

        when(urlRepository.findById(shortId)).thenReturn(url);

        Url result = urlService.getLongUrl(shortId);

        assertNotNull(result);
        assertEquals(url.getLongUrl(), result.getLongUrl());
        verify(urlRepository, times(1)).findById(shortId);
    }

    @Test
    void testGetLongUrl_NotFound() {
        String shortId = "abc123";

        when(urlRepository.findById(shortId)).thenReturn(null);

        Url result = urlService.getLongUrl(shortId);

        assertNull(result);
        verify(urlRepository, times(1)).findById(shortId);
    }
}