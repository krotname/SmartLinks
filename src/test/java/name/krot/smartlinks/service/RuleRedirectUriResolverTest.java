package name.krot.smartlinks.service;

import name.krot.smartlinks.model.Rule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleRedirectUriResolverTest {

    private final RuleRedirectUriResolver resolver = new RuleRedirectUriResolver();

    @Test
    void resolvesValidRedirectUrl() {
        assertEquals("https://otus.ru/ru", resolver.resolve(rule(" https://otus.ru/ru ")).orElseThrow().toString());
    }

    @Test
    void returnsEmptyForInvalidRedirectUrl() {
        assertTrue(resolver.resolve(rule("https:otus.ru/no-host")).isEmpty());
    }

    private static Rule rule(String redirectTo) {
        Rule rule = new Rule();
        rule.setRedirectTo(redirectTo);
        return rule;
    }
}
