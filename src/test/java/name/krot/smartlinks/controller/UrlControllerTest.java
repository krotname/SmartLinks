package name.krot.smartlinks.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import com.example.urlshortener.model.Url;
import com.example.urlshortener.service.UrlService;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UrlController.class)
public class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UrlService urlService;

    @Test
    public void testShortenUrl_Success() throws Exception {
        String longUrl = "https://www.example.com";
        String shortId = "abc123";

        when(urlService.shortenUrl(longUrl)).thenReturn(shortId);

        mockMvc.perform(post("/api/shorten")
                        .content(longUrl)
                        .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string("http://localhost/abc123"));
    }

    @Test
    public void testShortenUrl_InvalidUrl() throws Exception {
        String longUrl = "invalid_url";

        when(urlService.shortenUrl(longUrl)).thenThrow(new IllegalArgumentException("Invalid URL"));

        mockMvc.perform(post("/api/shorten")
                        .content(longUrl)
                        .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid URL"));
    }

    @Test
    public void testRedirect_Success() throws Exception {
        String shortId = "abc123";
        String longUrl = "https://www.example.com";
        Url url = new Url(shortId, longUrl, System.currentTimeMillis(), System.currentTimeMillis(), 0);

        when(urlService.getLongUrl(shortId)).thenReturn(url);

        mockMvc.perform(get("/" + shortId))
                .andExpect(status().isPermanentRedirect())
                .andExpect(header().string("Location", longUrl));
    }

    @Test
    public void testRedirect_NotFound() throws Exception {
        String shortId = "abc123";

        when(urlService.getLongUrl(shortId)).thenThrow(new ResourceNotFoundException("URL not found"));

        mockMvc.perform(get("/" + shortId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("URL not found"));
    }

    @Test
    public void testGetStatistics_Success() throws Exception {
        String shortId = "abc123";
        Url url = new Url(shortId, "https://www.example.com", System.currentTimeMillis(), System.currentTimeMillis(), 5);

        when(urlService.getUrlDetails(shortId)).thenReturn(url);

        mockMvc.perform(get("/api/statistics/" + shortId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(shortId))
                .andExpect(jsonPath("$.longUrl").value("https://www.example.com"))
                .andExpect(jsonPath("$.accessCount").value(5));
    }

    @Test
    public void testGetStatistics_NotFound() throws Exception {
        String shortId = "abc123";

        when(urlService.getUrlDetails(shortId)).thenThrow(new ResourceNotFoundException("URL not found"));

        mockMvc.perform(get("/api/statistics/" + shortId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("URL not found"));
    }
}