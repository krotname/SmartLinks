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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static name.krot.smartlinks.support.SmartLinkJsonFixtures.fallbackSmartLinkJson;
import static name.krot.smartlinks.support.SmartLinkJsonFixtures.invalidRedirectSmartLinkJson;
import static name.krot.smartlinks.support.SmartLinkJsonFixtures.validSmartLinkJson;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.RU_REDIRECT_URL;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.SMART_LINK_ID;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.requestContext;
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
        RequestContext context = requestContext("ru-RU");
        when(requestContextFactory.from(any())).thenReturn(context);
        when(redirectResolver.resolveRedirect(SMART_LINK_ID, context)).thenReturn(URI.create(RU_REDIRECT_URL));

        mockMvc.perform(get("/s/{smartLinkId}", SMART_LINK_ID)
                        .header("Accept-Language", "ru-RU"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", RU_REDIRECT_URL));
    }

    @Test
    void testRedirectWithNoMatchingRule() throws Exception {
        RequestContext context = requestContext("en-US");
        when(requestContextFactory.from(any())).thenReturn(context);
        when(redirectResolver.resolveRedirect(SMART_LINK_ID, context))
                .thenThrow(new NoMatchingRuleException("No matching rule found for this Smart Link"));

        mockMvc.perform(get("/s/{smartLinkId}", SMART_LINK_ID)
                        .header("Accept-Language", "en-US"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No matching rule found for this Smart Link"));
    }

    @Test
    void testRedirectWithNonExistentSmartLink() throws Exception {
        RequestContext context = requestContext(null);
        when(requestContextFactory.from(any())).thenReturn(context);
        when(redirectResolver.resolveRedirect("nonexistent", context))
                .thenThrow(new SmartLinkNotFoundException("Smart Link not found"));

        mockMvc.perform(get("/s/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Smart Link not found"));
    }

    @Test
    void testCreateSmartLink() throws Exception {
        mockMvc.perform(post("/api/smartlinks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validSmartLinkJson()))
                .andExpect(status().isCreated())
                .andExpect(content().string("Smart Link created successfully"));

        ArgumentCaptor<SmartLink> smartLinkCaptor = ArgumentCaptor.forClass(SmartLink.class);
        verify(smartLinkService, times(1)).saveSmartLink(smartLinkCaptor.capture());

        SmartLink capturedSmartLink = smartLinkCaptor.getValue();
        assertEquals(SMART_LINK_ID, capturedSmartLink.getId());
        assertEquals(1, capturedSmartLink.getRules().size());
    }

    @Test
    void testCreateSmartLinkAcceptsFallbackRule() throws Exception {
        mockMvc.perform(post("/api/smartlinks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fallbackSmartLinkJson()))
                .andExpect(status().isCreated());
    }

    @Test
    void testCreateSmartLinkRejectsRedirectWithoutHost() throws Exception {
        mockMvc.perform(post("/api/smartlinks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRedirectSmartLinkJson()))
                .andExpect(status().isBadRequest());
    }
}
