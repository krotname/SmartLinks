package name.krot.smartlinks.service;

import org.junit.jupiter.api.Test;

import static name.krot.smartlinks.support.SmartLinksTestFixtures.fallbackRule;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleRedirectUriResolverTest {

    private final RuleRedirectUriResolver resolver = new RuleRedirectUriResolver();

    @Test
    void resolvesValidRedirectUrl() {
        assertEquals("https://otus.ru/ru", resolver.resolve(fallbackRule(" https://otus.ru/ru ")).orElseThrow().toString());
    }

    @Test
    void returnsEmptyForInvalidRedirectUrl() {
        assertTrue(resolver.resolve(fallbackRule("https:otus.ru/no-host")).isEmpty());
    }
}
