package name.krot.smartlinks.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import name.krot.smartlinks.predicate.RequestContext;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class HttpRequestContextFactory implements RequestContextFactory {

    private final Clock clock;

    @Override
    public RequestContext from(HttpServletRequest request) {
        return new RequestContext(
                LocalDateTime.ofInstant(clock.instant(), clock.getZone()),
                request.getHeader("Accept-Language"),
                request.getHeader("User-Agent")
        );
    }
}
