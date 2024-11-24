package name.krot.smartlinks.command;

import name.krot.smartlinks.model.Rule;
import name.krot.smartlinks.predicate.Predicate;
import name.krot.smartlinks.predicate.PredicateFactory;
import name.krot.smartlinks.predicate.RequestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import org.mockito.*;

import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.*;

class RuleCommandTest {

    @Mock
    private PredicateFactory predicateFactory;

    @Mock
    private Predicate dateRangePredicate;

    @Mock
    private Predicate languagePredicate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRuleCommandWithMatchingPredicates() {
        RequestContext context = new RequestContext();
        context.setRequestTime(LocalDateTime.of(2024, 11, 15, 12, 0));
        context.setAcceptLanguage("ru-RU");

        Rule rule = new Rule();
        rule.setPredicates(Arrays.asList("DateRange", "Language"));
        Map<String, Object> args = new HashMap<>();
        args.put("startWith", "2024-11-01T00:00:00");
        args.put("endWith", "2024-12-01T00:00:00");
        args.put("language", Arrays.asList("ru", "ru-RU"));
        rule.setArgs(args);
        rule.setRedirectTo("https://otus.ru/ru");

        when(predicateFactory.createPredicate("DateRange")).thenReturn(dateRangePredicate);
        when(predicateFactory.createPredicate("Language")).thenReturn(languagePredicate);

        when(dateRangePredicate.evaluate(context, args)).thenReturn(true);
        when(languagePredicate.evaluate(context, args)).thenReturn(true);

        RuleCommand command = new RuleCommand(context, rule, predicateFactory);
        ResponseEntity<?> response = command.execute();

        assertNotNull(response);
        assertEquals(302, response.getStatusCodeValue());
        assertEquals("https://otus.ru/ru", response.getHeaders().getLocation().toString());
    }

    @Test
    void testRuleCommandWithNonMatchingPredicates() {
        RequestContext context = new RequestContext();
        context.setRequestTime(LocalDateTime.of(2024, 11, 15, 12, 0));
        context.setAcceptLanguage("en-US");

        Rule rule = new Rule();
        rule.setPredicates(Arrays.asList("DateRange", "Language"));
        Map<String, Object> args = new HashMap<>();
        args.put("startWith", "2024-11-01T00:00:00");
        args.put("endWith", "2024-12-01T00:00:00");
        args.put("language", Arrays.asList("ru", "ru-RU"));
        rule.setArgs(args);
        rule.setRedirectTo("https://otus.ru/ru");

        when(predicateFactory.createPredicate("DateRange")).thenReturn(dateRangePredicate);
        when(predicateFactory.createPredicate("Language")).thenReturn(languagePredicate);

        when(dateRangePredicate.evaluate(context, args)).thenReturn(true);
        when(languagePredicate.evaluate(context, args)).thenReturn(false);

        RuleCommand command = new RuleCommand(context, rule, predicateFactory);
        ResponseEntity<?> response = command.execute();

        assertNull(response);
    }
}