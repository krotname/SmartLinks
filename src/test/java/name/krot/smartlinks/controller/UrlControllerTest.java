package name.krot.smartlinks.controller;

import name.krot.smartlinks.exception.ResourceNotFoundException;
import name.krot.smartlinks.model.Url;
import name.krot.smartlinks.service.UrlService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled
@WebMvcTest(UrlController.class)
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private UrlService urlService;

    @Test
    void testShortenUrl_Success() throws Exception {
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
    void testShortenUrl_InvalidUrl() throws Exception {
        String longUrl = "invalid_url";

        when(urlService.shortenUrl(longUrl)).thenThrow(new IllegalArgumentException("Invalid URL"));

        mockMvc.perform(post("/api/shorten")
                        .content(longUrl)
                        .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid URL"));
    }

    @Test
    void testRedirect_Success() throws Exception {
        String shortId = "abc123";
        String longUrl = "https://www.example.com";
        Url url = new Url(shortId, longUrl, System.currentTimeMillis(), System.currentTimeMillis(), 0);

        when(urlService.getLongUrl(shortId)).thenReturn(url);

        mockMvc.perform(get("/" + shortId))
                .andExpect(status().isPermanentRedirect())
                .andExpect(header().string("Location", longUrl));
    }

    @Test
    void testRedirect_NotFound() throws Exception {
        String shortId = "abc123";

        when(urlService.getLongUrl(shortId)).thenThrow(new ResourceNotFoundException("URL not found"));

        mockMvc.perform(get("/" + shortId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("URL not found"));
    }

    @Test
    void testGetStatistics_Success() throws Exception {
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
    void testGetStatistics_NotFound() throws Exception {
        String shortId = "abc123";

        when(urlService.getUrlDetails(shortId)).thenThrow(new ResourceNotFoundException("URL not found"));

        mockMvc.perform(get("/api/statistics/" + shortId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("URL not found"));
    }
}