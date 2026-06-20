package name.krot.smartlinks.service;

import name.krot.smartlinks.model.Rule;
import name.krot.smartlinks.predicate.Predicate;
import name.krot.smartlinks.predicate.PredicateArguments;
import name.krot.smartlinks.predicate.PredicateFactory;
import name.krot.smartlinks.predicate.RequestContext;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AllPredicatesRuleEvaluatorTest {

    private final PredicateFactory predicateFactory = mock(PredicateFactory.class);
    private final Predicate dateRangePredicate = mock(Predicate.class);
    private final Predicate languagePredicate = mock(Predicate.class);
    private final AllPredicatesRuleEvaluator evaluator = new AllPredicatesRuleEvaluator(predicateFactory);

    @Test
    void matchesWhenAllPredicatesMatch() {
        RequestContext context = new RequestContext(LocalDateTime.of(2024, 11, 15, 12, 0), "ru-RU", null);
        Rule rule = rule(Arrays.asList("DateRange", "Language"));

        when(predicateFactory.createPredicate("DateRange")).thenReturn(dateRangePredicate);
        when(predicateFactory.createPredicate("Language")).thenReturn(languagePredicate);
        when(dateRangePredicate.evaluate(any(RequestContext.class), any(PredicateArguments.class))).thenReturn(true);
        when(languagePredicate.evaluate(any(RequestContext.class), any(PredicateArguments.class))).thenReturn(true);

        assertTrue(evaluator.matches(rule, context));
    }

    @Test
    void doesNotMatchWhenAnyPredicateFails() {
        RequestContext context = new RequestContext(LocalDateTime.of(2024, 11, 15, 12, 0), "en-US", null);
        Rule rule = rule(Arrays.asList("DateRange", "Language"));

        when(predicateFactory.createPredicate("DateRange")).thenReturn(dateRangePredicate);
        when(predicateFactory.createPredicate("Language")).thenReturn(languagePredicate);
        when(dateRangePredicate.evaluate(any(RequestContext.class), any(PredicateArguments.class))).thenReturn(true);
        when(languagePredicate.evaluate(any(RequestContext.class), any(PredicateArguments.class))).thenReturn(false);

        assertFalse(evaluator.matches(rule, context));
    }

    @Test
    void matchesFallbackRuleWithoutPredicates() {
        RequestContext context = new RequestContext(LocalDateTime.of(2024, 11, 15, 12, 0), "en-US", null);

        assertTrue(evaluator.matches(rule(List.of()), context));
    }

    private static Rule rule(List<String> predicates) {
        Map<String, Object> args = new HashMap<>();
        args.put("startWith", "2024-11-01T00:00:00");
        args.put("endWith", "2024-12-01T00:00:00");
        args.put("language", Arrays.asList("ru", "ru-RU"));

        Rule rule = new Rule();
        rule.setPredicates(predicates);
        rule.setArgs(args);
        rule.setRedirectTo("https://otus.ru/ru");
        return rule;
    }
}
