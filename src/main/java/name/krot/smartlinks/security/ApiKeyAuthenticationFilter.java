package name.krot.smartlinks.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.function.Predicate.not;

/**
 * Requires a shared API key on every state-changing request below {@value #PROTECTED_PATH_PREFIX}.
 *
 * <p>Creating a Smart Link mints a URL on a trusted domain that redirects anywhere, so an open
 * write endpoint is a stored open-redirect / phishing primitive and bypasses any redirect
 * allowlist enforced elsewhere. Reads stay public on purpose: {@code GET /s/{id}} is the product.
 *
 * <p>Behaviour:
 * <ul>
 *   <li>The key is read from {@code smartlinks.api-key} (environment: {@code SMARTLINKS_API_KEY}).</li>
 *   <li>An unset or blank key <strong>fails closed</strong>: every write is answered with 401.
 *       The application still boots and keeps serving redirects.</li>
 *   <li>A missing header and a wrong header produce the identical response, so the filter does not
 *       tell an attacker which of the two happened.</li>
 *   <li>Comparison goes through {@link MessageDigest#isEqual} to keep it constant-time.</li>
 * </ul>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1000)
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    /** Header carrying the shared key on write requests. */
    public static final String API_KEY_HEADER = "X-API-Key";

    /** Everything below this path is treated as the write surface. */
    public static final String PROTECTED_PATH_PREFIX = "/api/";

    static final String UNAUTHORIZED_BODY = "Unauthorized";

    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthenticationFilter.class);

    private static final String CHALLENGE = "ApiKey realm=\"SmartLinks\", header=\"" + API_KEY_HEADER + "\"";
    private static final String UNAUTHORIZED_CONTENT_TYPE = "text/plain;charset=UTF-8";

    /** Methods that cannot change state and therefore never need a key. */
    private static final Set<String> READ_ONLY_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    /** Path parameters (";foo=bar") are ignored by Spring MVC when routing, so ignore them here too. */
    private static final Pattern SEMICOLON_CONTENT = Pattern.compile(";[^/]*");

    /** Spring MVC collapses repeated slashes when routing, so "//api/x" must not slip past the prefix. */
    private static final Pattern REPEATED_SLASHES = Pattern.compile("/{2,}");

    /** UTF-8 bytes of the configured key, or {@code null} when no key is configured. */
    private final byte[] expectedApiKey;

    public ApiKeyAuthenticationFilter(@Value("${smartlinks.api-key:}") String apiKey) {
        this.expectedApiKey = Optional.ofNullable(apiKey)
                .filter(not(String::isBlank))
                .map(key -> key.getBytes(StandardCharsets.UTF_8))
                .orElse(null);

        Optional.ofNullable(this.expectedApiKey).ifPresentOrElse(
                key -> log.info("Write requests below {} require the {} header", PROTECTED_PATH_PREFIX, API_KEY_HEADER),
                () -> log.warn("smartlinks.api-key (env SMARTLINKS_API_KEY) is not set: every write below {} "
                        + "will be rejected with 401. Redirects stay public.", PROTECTED_PATH_PREFIX));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !requiresApiKey(request);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!isAuthorized(request.getHeader(API_KEY_HEADER))) {
            reject(request, response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    /**
     * A request needs a key when it can change state and targets the write surface.
     * The path is resolved <em>within the application</em>, so a non-root
     * {@code server.servlet.context-path} cannot make the prefix check fail open.
     */
    static boolean requiresApiKey(HttpServletRequest request) {
        return isStateChanging(request.getMethod())
                && pathWithinApplication(request).startsWith(PROTECTED_PATH_PREFIX);
    }

    private static boolean isStateChanging(String method) {
        return !READ_ONLY_METHODS.contains(Optional.ofNullable(method)
                .orElse("")
                .toUpperCase(Locale.ROOT));
    }

    /**
     * Returns the request path with the servlet context path removed and normalised the same way
     * Spring MVC normalises it before matching handler mappings.
     *
     * <p>Matching the raw {@link HttpServletRequest#getRequestURI()} against the prefix is the bug
     * this method exists to prevent: under {@code server.servlet.context-path=/redirector} the URI
     * is {@code /redirector/api/smartlinks}, which does not start with {@code /api/} — the guard
     * would silently pass every write through.
     */
    static String pathWithinApplication(HttpServletRequest request) {
        String requestUri = Optional.ofNullable(request.getRequestURI()).orElse("/");
        String contextPath = Optional.ofNullable(request.getContextPath()).orElse("");

        String withinApplication = Optional.of(contextPath)
                .filter(not(String::isEmpty))
                .filter(not("/"::equals))
                .filter(requestUri::startsWith)
                .map(prefix -> requestUri.substring(prefix.length()))
                // Only accept a clean segment boundary; otherwise keep the untouched URI.
                .filter(remainder -> remainder.isEmpty() || remainder.startsWith("/"))
                .orElse(requestUri);

        return normalize(withinApplication);
    }

    private static String normalize(String path) {
        String withoutPathParameters = SEMICOLON_CONTENT.matcher(path).replaceAll("");
        String collapsed = REPEATED_SLASHES.matcher(decode(withoutPathParameters)).replaceAll("/");
        return collapsed.isEmpty() ? "/" : collapsed;
    }

    private static String decode(String path) {
        try {
            return URLDecoder.decode(path, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException malformedEscape) {
            // Undecodable percent-escapes are rejected by the container anyway. Fall back to the
            // raw value rather than dropping the guard for a request we cannot interpret.
            return path;
        }
    }

    /**
     * {@link MessageDigest#isEqual} returns {@code false} when either array is {@code null},
     * so an unconfigured key ({@code expectedApiKey == null}) rejects every write.
     */
    private boolean isAuthorized(String presentedApiKey) {
        return Optional.ofNullable(presentedApiKey)
                .map(key -> key.getBytes(StandardCharsets.UTF_8))
                .filter(key -> MessageDigest.isEqual(expectedApiKey, key))
                .isPresent();
    }

    private static void reject(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.warn("Rejected unauthenticated {} {} from {}",
                request.getMethod(), pathWithinApplication(request), request.getRemoteAddr());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, CHALLENGE);
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setContentType(UNAUTHORIZED_CONTENT_TYPE);
        response.getWriter().write(UNAUTHORIZED_BODY);
    }
}
