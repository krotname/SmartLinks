package name.krot.smartlinks.service;

import name.krot.smartlinks.command.RedirectCommand;
import name.krot.smartlinks.command.RedirectCommandChain;
import name.krot.smartlinks.command.RedirectCommandFactory;
import name.krot.smartlinks.exception.NoMatchingRuleException;
import name.krot.smartlinks.exception.SmartLinkNotFoundException;
import name.krot.smartlinks.model.SmartLink;
import name.krot.smartlinks.predicate.RequestContext;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RuleBasedRedirectResolverTest {

    private final SmartLinkService smartLinkService = mock(SmartLinkService.class);
    private final RedirectCommandFactory redirectCommandFactory = mock(RedirectCommandFactory.class);
    private final RedirectCommandChain redirectCommandChain = mock(RedirectCommandChain.class);
    private final RuleBasedRedirectResolver resolver = new RuleBasedRedirectResolver(
            smartLinkService,
            redirectCommandFactory,
            redirectCommandChain
    );

    @Test
    void resolvesRedirectForExistingSmartLink() {
        SmartLink smartLink = new SmartLink();
        smartLink.setId("smartlink123");
        RequestContext context = new RequestContext(LocalDateTime.now(), "ru-RU", null);
        URI redirectUri = URI.create("https://otus.ru/ru");
        List<RedirectCommand> commands = List.of(ignored -> Optional.of(redirectUri));

        when(smartLinkService.findSmartLinkById("smartlink123")).thenReturn(Optional.of(smartLink));
        when(redirectCommandFactory.createCommands(smartLink)).thenReturn(commands);
        when(redirectCommandChain.execute(commands, context)).thenReturn(Optional.of(redirectUri));

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
        when(redirectCommandFactory.createCommands(smartLink)).thenReturn(List.of());
        when(redirectCommandChain.execute(List.of(), context)).thenReturn(Optional.empty());

        assertThrows(NoMatchingRuleException.class, () -> resolver.resolveRedirect("smartlink123", context));
    }
}
