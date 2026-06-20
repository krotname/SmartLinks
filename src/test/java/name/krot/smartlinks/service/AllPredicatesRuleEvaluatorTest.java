package name.krot.smartlinks.service;

import name.krot.smartlinks.model.Rule;
import name.krot.smartlinks.predicate.Predicate;
import name.krot.smartlinks.predicate.PredicateArguments;
import name.krot.smartlinks.predicate.PredicateFactory;
import name.krot.smartlinks.predicate.RequestContext;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static name.krot.smartlinks.support.SmartLinksTestFixtures.RU_REDIRECT_URL;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.requestContext;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.rule;
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
        RequestContext context = requestContext("ru-RU");
        Rule rule = ruleWithPredicates(Arrays.asList("DateRange", "Language"));

        when(predicateFactory.createPredicate("DateRange")).thenReturn(dateRangePredicate);
        when(predicateFactory.createPredicate("Language")).thenReturn(languagePredicate);
        when(dateRangePredicate.evaluate(any(RequestContext.class), any(PredicateArguments.class))).thenReturn(true);
        when(languagePredicate.evaluate(any(RequestContext.class), any(PredicateArguments.class))).thenReturn(true);

        assertTrue(evaluator.matches(rule, context));
    }

    @Test
    void doesNotMatchWhenAnyPredicateFails() {
        RequestContext context = requestContext("en-US");
        Rule rule = ruleWithPredicates(Arrays.asList("DateRange", "Language"));

        when(predicateFactory.createPredicate("DateRange")).thenReturn(dateRangePredicate);
        when(predicateFactory.createPredicate("Language")).thenReturn(languagePredicate);
        when(dateRangePredicate.evaluate(any(RequestContext.class), any(PredicateArguments.class))).thenReturn(true);
        when(languagePredicate.evaluate(any(RequestContext.class), any(PredicateArguments.class))).thenReturn(false);

        assertFalse(evaluator.matches(rule, context));
    }

    @Test
    void matchesFallbackRuleWithoutPredicates() {
        RequestContext context = requestContext("en-US");

        assertTrue(evaluator.matches(ruleWithPredicates(List.of()), context));
    }

    private static Rule ruleWithPredicates(List<String> predicates) {
        return rule(predicates, RU_REDIRECT_URL);
    }
}
