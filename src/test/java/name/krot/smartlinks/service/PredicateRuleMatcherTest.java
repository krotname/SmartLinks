package name.krot.smartlinks.service;

import name.krot.smartlinks.model.Rule;
import name.krot.smartlinks.model.SmartLink;
import name.krot.smartlinks.predicate.Predicate;
import name.krot.smartlinks.predicate.PredicateArguments;
import name.krot.smartlinks.predicate.PredicateFactory;
import name.krot.smartlinks.predicate.RequestContext;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PredicateRuleMatcherTest {

    private final PredicateFactory predicateFactory = mock(PredicateFactory.class);
    private final Predicate dateRangePredicate = mock(Predicate.class);
    private final Predicate languagePredicate = mock(Predicate.class);
    private final PredicateRuleMatcher matcher = new PredicateRuleMatcher(predicateFactory);

    @Test
    void returnsRedirectForFirstMatchingRule() {
        RequestContext context = new RequestContext(LocalDateTime.of(2024, 11, 15, 12, 0), "ru-RU", null);
        Rule rule = rule(Arrays.asList("DateRange", "Language"), "https://otus.ru/ru");
        SmartLink smartLink = smartLink(rule);

        when(predicateFactory.createPredicate("DateRange")).thenReturn(dateRangePredicate);
        when(predicateFactory.createPredicate("Language")).thenReturn(languagePredicate);
        when(dateRangePredicate.evaluate(any(RequestContext.class), any(PredicateArguments.class))).thenReturn(true);
        when(languagePredicate.evaluate(any(RequestContext.class), any(PredicateArguments.class))).thenReturn(true);

        Optional<URI> result = matcher.findRedirect(smartLink, context);

        assertTrue(result.isPresent());
        assertEquals("https://otus.ru/ru", result.orElseThrow().toString());
    }

    @Test
    void returnsEmptyWhenPredicatesDoNotMatch() {
        RequestContext context = new RequestContext(LocalDateTime.of(2024, 11, 15, 12, 0), "en-US", null);
        Rule rule = rule(Arrays.asList("DateRange", "Language"), "https://otus.ru/ru");
        SmartLink smartLink = smartLink(rule);

        when(predicateFactory.createPredicate("DateRange")).thenReturn(dateRangePredicate);
        when(predicateFactory.createPredicate("Language")).thenReturn(languagePredicate);
        when(dateRangePredicate.evaluate(any(RequestContext.class), any(PredicateArguments.class))).thenReturn(true);
        when(languagePredicate.evaluate(any(RequestContext.class), any(PredicateArguments.class))).thenReturn(false);

        assertTrue(matcher.findRedirect(smartLink, context).isEmpty());
    }

    @Test
    void supportsFallbackRuleWithoutPredicates() {
        RequestContext context = new RequestContext(LocalDateTime.of(2024, 11, 15, 12, 0), "en-US", null);
        Rule rule = rule(List.of(), "https://otus.ru/default");
        SmartLink smartLink = smartLink(rule);

        Optional<URI> result = matcher.findRedirect(smartLink, context);

        assertTrue(result.isPresent());
        assertEquals("https://otus.ru/default", result.orElseThrow().toString());
    }

    @Test
    void skipsInvalidStoredRedirectUrl() {
        RequestContext context = new RequestContext(LocalDateTime.of(2024, 11, 15, 12, 0), "ru-RU", null);
        Rule rule = rule(List.of(), "https:otus.ru/no-host");
        SmartLink smartLink = smartLink(rule);

        assertTrue(matcher.findRedirect(smartLink, context).isEmpty());
    }

    private static SmartLink smartLink(Rule rule) {
        SmartLink smartLink = new SmartLink();
        smartLink.setId("smartlink123");
        smartLink.setRules(List.of(rule));
        return smartLink;
    }

    private static Rule rule(List<String> predicates, String redirectTo) {
        Map<String, Object> args = new HashMap<>();
        args.put("startWith", "2024-11-01T00:00:00");
        args.put("endWith", "2024-12-01T00:00:00");
        args.put("language", Arrays.asList("ru", "ru-RU"));

        Rule rule = new Rule();
        rule.setPredicates(predicates);
        rule.setArgs(args);
        rule.setRedirectTo(redirectTo);
        return rule;
    }
}
