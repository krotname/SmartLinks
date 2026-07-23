package name.krot.smartlinks.controller;

import name.krot.smartlinks.exception.NoMatchingRuleException;
import name.krot.smartlinks.exception.SmartLinkNotFoundException;
import name.krot.smartlinks.exception.SmartLinkAlreadyExistsException;
import name.krot.smartlinks.model.SmartLink;
import name.krot.smartlinks.predicate.RequestContext;
import name.krot.smartlinks.service.RedirectResolver;
import name.krot.smartlinks.service.SmartLinkService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static name.krot.smartlinks.security.ApiKeyAuthenticationFilter.API_KEY_HEADER;
import static name.krot.smartlinks.support.SmartLinkJsonFixtures.fallbackSmartLinkJson;
import static name.krot.smartlinks.support.SmartLinkJsonFixtures.invalidRedirectSmartLinkJson;
import static name.krot.smartlinks.support.SmartLinkJsonFixtures.validSmartLinkJson;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.API_KEY;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.RU_REDIRECT_URL;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.SMART_LINK_ID;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.requestContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = RedirectController.class, properties = "smartlinks.api-key=" + API_KEY)
class RedirectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SmartLinkService smartLinkService;

    @MockitoBean
    private RedirectResolver redirectResolver;

    @MockitoBean
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
    void redisOutageIsReportedAsRetryableServiceUnavailable() throws Exception {
        RequestContext context = requestContext(null);
        when(requestContextFactory.from(any())).thenReturn(context);
        when(redirectResolver.resolveRedirect(SMART_LINK_ID, context))
                .thenThrow(new DataAccessResourceFailureException("redis unavailable"));

        mockMvc.perform(get("/s/{smartLinkId}", SMART_LINK_ID))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().string("Retry-After", "1"));
    }

    @Test
    void redirectRejectsInvalidOrOversizedIdsBeforeRepositoryLookup() throws Exception {
        mockMvc.perform(get("/s/{smartLinkId}", "bad$id"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/s/{smartLinkId}", "a".repeat(129)))
                .andExpect(status().isBadRequest());

        verify(redirectResolver, never()).resolveRedirect(any(), any());
    }

    @Test
    void unknownRouteKeepsFrameworkNotFoundStatus() throws Exception {
        mockMvc.perform(get("/does-not-exist"))
                .andExpect(status().isNotFound());
    }

    @Test
    void methodNotAllowedKeepsFrameworkAllowHeader() throws Exception {
        mockMvc.perform(put("/api/smartlinks")
                        .header(API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validSmartLinkJson()))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", "POST"));
    }

    @Test
    void testCreateSmartLink() throws Exception {
        mockMvc.perform(post("/api/smartlinks")
                        .header(API_KEY_HEADER, API_KEY)
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
                        .header(API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fallbackSmartLinkJson()))
                .andExpect(status().isCreated());
    }

    @Test
    void testCreateSmartLinkRejectsDuplicateIdWithoutOverwrite() throws Exception {
        doThrow(new SmartLinkAlreadyExistsException("Smart Link already exists: " + SMART_LINK_ID))
                .when(smartLinkService).saveSmartLink(any());

        mockMvc.perform(post("/api/smartlinks")
                        .header(API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validSmartLinkJson()))
                .andExpect(status().isConflict());
    }

    @Test
    void testCreateSmartLinkRejectsRedirectWithoutHost() throws Exception {
        mockMvc.perform(post("/api/smartlinks")
                        .header(API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRedirectSmartLinkJson()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRejectsDuplicateJsonProperties() throws Exception {
        String duplicateId = fallbackSmartLinkJson().replace(
                "\"id\": \"smartlink123\"",
                "\"id\": \"smartlink123\", \"id\": \"ambiguous\"");

        mockMvc.perform(post("/api/smartlinks")
                        .header(API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateId))
                .andExpect(status().isBadRequest());

        verify(smartLinkService, never()).saveSmartLink(any());
    }

    @Test
    void createRejectsUnknownJsonProperties() throws Exception {
        String unknownProperty = fallbackSmartLinkJson().replace(
                "\"id\": \"smartlink123\"",
                "\"id\": \"smartlink123\", \"idd\": \"typo\"");

        mockMvc.perform(post("/api/smartlinks")
                        .header(API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(unknownProperty))
                .andExpect(status().isBadRequest());

        verify(smartLinkService, never()).saveSmartLink(any());
    }

    @Test
    void createWithoutApiKeyIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/smartlinks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validSmartLinkJson()))
                .andExpect(status().isUnauthorized());

        verify(smartLinkService, never()).saveSmartLink(any());
    }

    @Test
    void createWithWrongApiKeyIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/smartlinks")
                        .header(API_KEY_HEADER, API_KEY + "-tampered")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validSmartLinkJson()))
                .andExpect(status().isUnauthorized());

        verify(smartLinkService, never()).saveSmartLink(any());
    }

    @Test
    void redirectStaysPublicWithoutApiKey() throws Exception {
        RequestContext context = requestContext("ru-RU");
        when(requestContextFactory.from(any())).thenReturn(context);
        when(redirectResolver.resolveRedirect(SMART_LINK_ID, context)).thenReturn(URI.create(RU_REDIRECT_URL));

        mockMvc.perform(get("/s/{smartLinkId}", SMART_LINK_ID)
                        .header("Accept-Language", "ru-RU"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", RU_REDIRECT_URL));
    }

    /**
     * Guards the regression the filter is built around: stripping the servlet context path before
     * matching "/api/". Without it the guard silently passes writes under a non-root context path.
     */
    @Test
    void createUnderNonRootContextPathStillRequiresApiKey() throws Exception {
        mockMvc.perform(post("/redirector/api/smartlinks")
                        .contextPath("/redirector")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validSmartLinkJson()))
                .andExpect(status().isUnauthorized());

        verify(smartLinkService, never()).saveSmartLink(any());
    }

    @Test
    void createUnderNonRootContextPathSucceedsWithApiKey() throws Exception {
        mockMvc.perform(post("/redirector/api/smartlinks")
                        .contextPath("/redirector")
                        .header(API_KEY_HEADER, API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validSmartLinkJson()))
                .andExpect(status().isCreated());

        verify(smartLinkService, times(1)).saveSmartLink(any());
    }
}
