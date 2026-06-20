package name.krot.smartlinks.command;

import name.krot.smartlinks.model.Rule;
import name.krot.smartlinks.predicate.RequestContext;
import name.krot.smartlinks.service.PredicateRuleEvaluator;
import name.krot.smartlinks.service.RedirectUriResolver;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RuleRedirectCommandTest {

    private final PredicateRuleEvaluator predicateRuleEvaluator = mock(PredicateRuleEvaluator.class);
    private final RedirectUriResolver redirectUriResolver = mock(RedirectUriResolver.class);

    @Test
    void returnsRedirectWhenRuleMatches() {
        Rule rule = rule();
        RequestContext context = new RequestContext(LocalDateTime.now(), "ru-RU", null);
        URI redirectUri = URI.create("https://otus.ru/ru");
        RuleRedirectCommand command = new RuleRedirectCommand(rule, predicateRuleEvaluator, redirectUriResolver);

        when(predicateRuleEvaluator.matches(rule, context)).thenReturn(true);
        when(redirectUriResolver.resolve(rule)).thenReturn(Optional.of(redirectUri));

        Optional<URI> result = command.execute(context);

        assertTrue(result.isPresent());
        assertEquals(redirectUri, result.orElseThrow());
    }

    @Test
    void returnsEmptyWhenRuleDoesNotMatch() {
        Rule rule = rule();
        RequestContext context = new RequestContext(LocalDateTime.now(), "en-US", null);
        RuleRedirectCommand command = new RuleRedirectCommand(rule, predicateRuleEvaluator, redirectUriResolver);

        when(predicateRuleEvaluator.matches(rule, context)).thenReturn(false);

        assertTrue(command.execute(context).isEmpty());
    }

    private static Rule rule() {
        Rule rule = new Rule();
        rule.setPredicates(List.of("Language"));
        rule.setRedirectTo("https://otus.ru/ru");
        return rule;
    }
}
