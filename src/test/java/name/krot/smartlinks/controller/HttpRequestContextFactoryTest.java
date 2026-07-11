package name.krot.smartlinks.controller;

import name.krot.smartlinks.predicate.RequestContext;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpRequestContextFactoryTest {

    @Test
    void buildsRequestContextFromHttpRequest() {
        Clock clock = Clock.fixed(Instant.parse("2024-11-15T12:00:00Z"), ZoneId.of("UTC"));
        HttpRequestContextFactory factory = new HttpRequestContextFactory(clock);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Accept-Language", "ru-RU");
        request.addHeader("User-Agent", "Mozilla/5.0");

        RequestContext context = factory.from(request);

        assertEquals(LocalDateTime.of(2024, 11, 15, 12, 0), context.requestTime());
        assertEquals("ru-RU", context.acceptLanguage());
        assertEquals("Mozilla/5.0", context.userAgent());
    }

    @Test
    void combinesRepeatedAcceptLanguageHeaders() {
        Clock clock = Clock.fixed(Instant.parse("2024-11-15T12:00:00Z"), ZoneId.of("UTC"));
        HttpRequestContextFactory factory = new HttpRequestContextFactory(clock);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Accept-Language", "en-US;q=0.5");
        request.addHeader("Accept-Language", "ru-RU;q=0.9");

        RequestContext context = factory.from(request);

        assertEquals("en-US;q=0.5,ru-RU;q=0.9", context.acceptLanguage());
    }
}
