package name.krot.smartlinks.service;

import name.krot.smartlinks.exception.NoMatchingRuleException;
import name.krot.smartlinks.exception.SmartLinkNotFoundException;
import name.krot.smartlinks.model.SmartLink;
import name.krot.smartlinks.predicate.RequestContext;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RuleBasedRedirectResolverTest {

    private final SmartLinkService smartLinkService = mock(SmartLinkService.class);
    private final RuleMatcher ruleMatcher = mock(RuleMatcher.class);
    private final RuleBasedRedirectResolver resolver = new RuleBasedRedirectResolver(smartLinkService, ruleMatcher);

    @Test
    void resolvesRedirectForExistingSmartLink() {
        SmartLink smartLink = new SmartLink();
        smartLink.setId("smartlink123");
        RequestContext context = new RequestContext(LocalDateTime.now(), "ru-RU", null);
        URI redirectUri = URI.create("https://otus.ru/ru");

        when(smartLinkService.findSmartLinkById("smartlink123")).thenReturn(Optional.of(smartLink));
        when(ruleMatcher.findRedirect(smartLink, context)).thenReturn(Optional.of(redirectUri));

        assertEquals(redirectUri, resolver.resolveRedirect("smartlink123", context));
    }

    @Test
    void throwsWhenSmartLinkDoesNotExist() {
        RequestContext context = new RequestContext(LocalDateTime.now(), null, null);
        when(smartLinkService.findSmartLinkById("missing")).thenReturn(Optional.empty());

        assertThrows(SmartLinkNotFoundException.class, () -> resolver.resolveRedirect("missing", context));
    }

    @Test
    void throwsWhenNoRuleMatches() {
        SmartLink smartLink = new SmartLink();
        smartLink.setId("smartlink123");
        RequestContext context = new RequestContext(LocalDateTime.now(), "en-US", null);

        when(smartLinkService.findSmartLinkById("smartlink123")).thenReturn(Optional.of(smartLink));
        when(ruleMatcher.findRedirect(smartLink, context)).thenReturn(Optional.empty());

        assertThrows(NoMatchingRuleException.class, () -> resolver.resolveRedirect("smartlink123", context));
    }
}
