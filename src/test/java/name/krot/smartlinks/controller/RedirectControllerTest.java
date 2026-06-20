package name.krot.smartlinks.controller;

import name.krot.smartlinks.exception.NoMatchingRuleException;
import name.krot.smartlinks.exception.SmartLinkNotFoundException;
import name.krot.smartlinks.model.SmartLink;
import name.krot.smartlinks.predicate.RequestContext;
import name.krot.smartlinks.service.RedirectResolver;
import name.krot.smartlinks.service.SmartLinkService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RedirectController.class)
class RedirectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SmartLinkService smartLinkService;

    @MockBean
    private RedirectResolver redirectResolver;

    @MockBean
    private RequestContextFactory requestContextFactory;

    @Test
    void testRedirectWithMatchingRule() throws Exception {
        RequestContext context = new RequestContext(LocalDateTime.of(2024, 11, 15, 12, 0), "ru-RU", null);
        when(requestContextFactory.from(any())).thenReturn(context);
        when(redirectResolver.resolveRedirect("smartlink123", context)).thenReturn(URI.create("https://otus.ru/ru"));

        mockMvc.perform(get("/s/smartlink123")
                        .header("Accept-Language", "ru-RU"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://otus.ru/ru"));
    }

    @Test
    void testRedirectWithNoMatchingRule() throws Exception {
        RequestContext context = new RequestContext(LocalDateTime.of(2024, 11, 15, 12, 0), "en-US", null);
        when(requestContextFactory.from(any())).thenReturn(context);
        when(redirectResolver.resolveRedirect("smartlink123", context))
                .thenThrow(new NoMatchingRuleException("No matching rule found for this Smart Link"));

        mockMvc.perform(get("/s/smartlink123")
                        .header("Accept-Language", "en-US"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No matching rule found for this Smart Link"));
    }

    @Test
    void testRedirectWithNonExistentSmartLink() throws Exception {
        RequestContext context = new RequestContext(LocalDateTime.of(2024, 11, 15, 12, 0), null, null);
        when(requestContextFactory.from(any())).thenReturn(context);
        when(redirectResolver.resolveRedirect("nonexistent", context))
                .thenThrow(new SmartLinkNotFoundException("Smart Link not found"));

        mockMvc.perform(get("/s/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Smart Link not found"));
    }

    @Test
    void testCreateSmartLink() throws Exception {
        String smartLinkJson = "{\n" +
                "  \"id\": \"smartlink123\",\n" +
                "  \"rules\": [\n" +
                "    {\n" +
                "      \"predicates\": [\"DateRange\", \"Language\"],\n" +
                "      \"args\": {\n" +
                "        \"startWith\": \"2024-11-01T00:00:00\",\n" +
                "        \"endWith\": \"2024-12-01T00:00:00\",\n" +
                "        \"language\": [\"ru\", \"ru-RU\"]\n" +
                "      },\n" +
                "      \"redirectTo\": \"https://otus.ru/ru\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        mockMvc.perform(post("/api/smartlinks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(smartLinkJson))
                .andExpect(status().isCreated())
                .andExpect(content().string("Smart Link created successfully"));

        ArgumentCaptor<SmartLink> smartLinkCaptor = ArgumentCaptor.forClass(SmartLink.class);
        verify(smartLinkService, times(1)).saveSmartLink(smartLinkCaptor.capture());

        SmartLink capturedSmartLink = smartLinkCaptor.getValue();
        assertEquals("smartlink123", capturedSmartLink.getId());
        assertEquals(1, capturedSmartLink.getRules().size());
    }

    @Test
    void testCreateSmartLinkAcceptsFallbackRule() throws Exception {
        String smartLinkJson = "{\n" +
                "  \"id\": \"smartlink123\",\n" +
                "  \"rules\": [\n" +
                "    {\n" +
                "      \"predicates\": [],\n" +
                "      \"args\": {},\n" +
                "      \"redirectTo\": \"https://otus.ru/default\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        mockMvc.perform(post("/api/smartlinks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(smartLinkJson))
                .andExpect(status().isCreated());
    }

    @Test
    void testCreateSmartLinkRejectsRedirectWithoutHost() throws Exception {
        String smartLinkJson = "{\n" +
                "  \"id\": \"smartlink123\",\n" +
                "  \"rules\": [\n" +
                "    {\n" +
                "      \"predicates\": [\"Language\"],\n" +
                "      \"args\": {\n" +
                "        \"language\": [\"ru\"]\n" +
                "      },\n" +
                "      \"redirectTo\": \"https:otus.ru/no-host\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        mockMvc.perform(post("/api/smartlinks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(smartLinkJson))
                .andExpect(status().isBadRequest());
    }
}
