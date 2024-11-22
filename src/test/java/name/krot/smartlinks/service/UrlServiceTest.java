package name.krot.smartlinks.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import com.example.urlshortener.model.Url;
import com.example.urlshortener.repository.UrlRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UrlServiceTest {

    @InjectMocks
    private UrlService urlService;

    @Mock
    private UrlRepository urlRepository;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testShortenUrl_ValidUrl() {
        String longUrl = "https://www.example.com";
        when(urlRepository.findById(anyString())).thenReturn(null);

        String shortId = urlService.shortenUrl(longUrl);

        assertNotNull(shortId);
        verify(urlRepository, times(1)).save(any(Url.class));
    }

    @Test
    public void testShortenUrl_InvalidUrl() {
        String longUrl = "invalid_url";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            urlService.shortenUrl(longUrl);
        });

        assertEquals("Invalid URL", exception.getMessage());
    }

    @Test
    public void testGetLongUrl_Found() {
        String shortId = "abc123";
        Url url = new Url(shortId, "https://www.example.com", System.currentTimeMillis(), System.currentTimeMillis(), 0);

        when(urlRepository.findById(shortId)).thenReturn(url);

        Url result = urlService.getLongUrl(shortId);

        assertNotNull(result);
        assertEquals(url.getLongUrl(), result.getLongUrl());
        verify(urlRepository, times(1)).findById(shortId);
    }

    @Test
    public void testGetLongUrl_NotFound() {
        String shortId = "abc123";

        when(urlRepository.findById(shortId)).thenReturn(null);

        Url result = urlService.getLongUrl(shortId);

        assertNull(result);
        verify(urlRepository, times(1)).findById(shortId);
    }
}