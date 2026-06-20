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
import java.util.List;
import java.util.Optional;

import static name.krot.smartlinks.support.SmartLinksTestFixtures.RU_REDIRECT_URL;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.SMART_LINK_ID;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.requestContext;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.smartLink;
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
        SmartLink smartLink = smartLink();
        RequestContext context = requestContext("ru-RU");
        URI redirectUri = URI.create(RU_REDIRECT_URL);
        List<RedirectCommand> commands = List.of(ignored -> Optional.of(redirectUri));

        when(smartLinkService.findSmartLinkById(SMART_LINK_ID)).thenReturn(Optional.of(smartLink));
        when(redirectCommandFactory.createCommands(smartLink)).thenReturn(commands);
        when(redirectCommandChain.execute(commands, context)).thenReturn(Optional.of(redirectUri));

        assertEquals(redirectUri, resolver.resolveRedirect(SMART_LINK_ID, context));
    }

    @Test
    void throwsWhenSmartLinkDoesNotExist() {
        RequestContext context = requestContext(null);
        when(smartLinkService.findSmartLinkById("missing")).thenReturn(Optional.empty());

        assertThrows(SmartLinkNotFoundException.class, () -> resolver.resolveRedirect("missing", context));
    }

    @Test
    void throwsWhenNoRuleMatches() {
        SmartLink smartLink = smartLink();
        RequestContext context = requestContext("en-US");

        when(smartLinkService.findSmartLinkById(SMART_LINK_ID)).thenReturn(Optional.of(smartLink));
        when(redirectCommandFactory.createCommands(smartLink)).thenReturn(List.of());
        when(redirectCommandChain.execute(List.of(), context)).thenReturn(Optional.empty());

        assertThrows(NoMatchingRuleException.class, () -> resolver.resolveRedirect(SMART_LINK_ID, context));
    }
}
