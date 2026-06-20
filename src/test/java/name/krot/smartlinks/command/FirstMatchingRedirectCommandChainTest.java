package name.krot.smartlinks.command;

import name.krot.smartlinks.predicate.RequestContext;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static name.krot.smartlinks.support.SmartLinksTestFixtures.requestContext;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirstMatchingRedirectCommandChainTest {

    private final FirstMatchingRedirectCommandChain chain = new FirstMatchingRedirectCommandChain();

    @Test
    void returnsFirstCommandResult() {
        RequestContext context = requestContext("ru-RU");
        URI redirectUri = URI.create("https://otus.ru/ru");

        Optional<URI> result = chain.execute(List.of(
                ignored -> Optional.empty(),
                ignored -> Optional.of(redirectUri),
                ignored -> Optional.of(URI.create("https://otus.ru/default"))
        ), context);

        assertTrue(result.isPresent());
        assertEquals(redirectUri, result.orElseThrow());
    }

    @Test
    void returnsEmptyWhenNoCommandMatches() {
        RequestContext context = requestContext("en-US");

        assertTrue(chain.execute(List.of(ignored -> Optional.empty()), context).isEmpty());
    }
}
