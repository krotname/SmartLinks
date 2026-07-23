package name.krot.smartlinks.security;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static name.krot.smartlinks.security.ApiKeyAuthenticationFilter.API_KEY_HEADER;
import static name.krot.smartlinks.security.ApiKeyAuthenticationFilter.UNAUTHORIZED_BODY;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.API_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ApiKeyAuthenticationFilterTest {

    private static final String CONTEXT_PATH = "/redirector";

    private final ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter(API_KEY);
    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final MockFilterChain chain = new MockFilterChain();

    @Test
    void writeWithoutApiKeyIsRejected() throws Exception {
        invoke(filter, post("/api/smartlinks"));

        assertUnauthorized();
    }

    @Test
    void writeWithWrongApiKeyIsRejected() throws Exception {
        MockHttpServletRequest request = post("/api/smartlinks");
        request.addHeader(API_KEY_HEADER, API_KEY + "-tampered");

        invoke(filter, request);

        assertUnauthorized();
    }

    @Test
    void writeWithCorrectApiKeyReachesTheApplication() throws Exception {
        MockHttpServletRequest request = post("/api/smartlinks");
        request.addHeader(API_KEY_HEADER, API_KEY);

        invoke(filter, request);

        assertChainInvoked();
    }

    @Test
    void redirectStaysPublic() throws Exception {
        invoke(filter, get("/s/smartlink123"));

        assertChainInvoked();
    }

    @Test
    void actuatorAndDocsStayPublic() throws Exception {
        invoke(filter, get("/actuator/health/readiness"));

        assertChainInvoked();
    }

    /**
     * The trap this filter is written around: matching the raw request URI against "/api/" fails
     * open once the application is deployed under a non-root servlet context path.
     */
    @Test
    void contextPathIsStrippedBeforeThePrefixIsMatched() throws Exception {
        MockHttpServletRequest request = post(CONTEXT_PATH + "/api/smartlinks");
        request.setContextPath(CONTEXT_PATH);

        invoke(filter, request);

        assertUnauthorized();
    }

    @Test
    void contextPathWriteWithCorrectApiKeyReachesTheApplication() throws Exception {
        MockHttpServletRequest request = post(CONTEXT_PATH + "/api/smartlinks");
        request.setContextPath(CONTEXT_PATH);
        request.addHeader(API_KEY_HEADER, API_KEY);

        invoke(filter, request);

        assertChainInvoked();
    }

    @Test
    void contextPathRedirectStaysPublic() throws Exception {
        MockHttpServletRequest request = get(CONTEXT_PATH + "/s/smartlink123");
        request.setContextPath(CONTEXT_PATH);

        invoke(filter, request);

        assertChainInvoked();
    }

    @Test
    void blankApiKeyFailsClosedInsteadOfAllowingEveryWrite() throws Exception {
        ApiKeyAuthenticationFilter unconfigured = new ApiKeyAuthenticationFilter("   ");
        MockHttpServletRequest request = post("/api/smartlinks");
        request.addHeader(API_KEY_HEADER, "");

        invoke(unconfigured, request);

        assertUnauthorized();
    }

    @Test
    void repeatedSlashesDoNotBypassThePrefix() throws Exception {
        invoke(filter, post("//api/smartlinks"));

        assertUnauthorized();
    }

    @Test
    void pathParametersDoNotBypassThePrefix() throws Exception {
        invoke(filter, post("/api;jsessionid=42/smartlinks"));

        assertUnauthorized();
    }

    @Test
    void percentEncodedPathDoesNotBypassThePrefix() throws Exception {
        invoke(filter, post("/%61pi/smartlinks"));

        assertUnauthorized();
    }

    @Test
    void everyStateChangingMethodOnTheWriteSurfaceIsGuarded() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/smartlinks/smartlink123");

        invoke(filter, request);

        assertUnauthorized();
    }

    private void assertUnauthorized() throws IOException {
        assertEquals(401, response.getStatus());
        assertEquals(UNAUTHORIZED_BODY, response.getContentAsString());
        assertNotNull(response.getHeader(HttpHeaders.WWW_AUTHENTICATE));
        assertNull(chain.getRequest(), "Rejected request must not reach the application");
    }

    private void assertChainInvoked() {
        assertNotNull(chain.getRequest(), "Request should have been passed down the filter chain");
        assertEquals(200, response.getStatus());
    }

    private void invoke(ApiKeyAuthenticationFilter target, MockHttpServletRequest request)
            throws ServletException, IOException {
        target.doFilter(request, response, chain);
    }

    private static MockHttpServletRequest post(String requestUri) {
        return new MockHttpServletRequest("POST", requestUri);
    }

    private static MockHttpServletRequest get(String requestUri) {
        return new MockHttpServletRequest("GET", requestUri);
    }
}
