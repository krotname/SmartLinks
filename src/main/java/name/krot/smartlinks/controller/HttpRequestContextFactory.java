package name.krot.smartlinks.controller;

import jakarta.servlet.http.HttpServletRequest;
import name.krot.smartlinks.predicate.RequestContext;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class HttpRequestContextFactory implements RequestContextFactory {

    private final Clock clock;

    public HttpRequestContextFactory(Clock clock) {
        this.clock = clock;
    }

    @Override
    public RequestContext from(HttpServletRequest request) {
        return new RequestContext(
                LocalDateTime.ofInstant(clock.instant(), clock.getZone()),
                combinedHeader(request, "Accept-Language"),
                request.getHeader("User-Agent")
        );
    }

    private static String combinedHeader(HttpServletRequest request, String name) {
        String value = Collections.list(request.getHeaders(name)).stream()
                .filter(header -> header != null && !header.isBlank())
                .collect(Collectors.joining(","));
        return value.isEmpty() ? null : value;
    }
}
