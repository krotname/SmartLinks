package name.krot.smartlinks.predicate;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class LanguagePredicateTest {

    private final LanguagePredicate languagePredicate = new LanguagePredicate();

    @Test
    void testLanguageMatches() {
        RequestContext context = new RequestContext(LocalDateTime.now(), "ru-RU", null);

        Map<String, Object> args = new HashMap<>();
        args.put("language", Arrays.asList("ru", "ru-RU"));

        boolean result = languagePredicate.evaluate(context, new PredicateArguments(args));
        assertTrue(result);
    }

    @Test
    void testLanguageDoesNotMatch() {
        RequestContext context = new RequestContext(LocalDateTime.now(), "en-US", null);

        Map<String, Object> args = new HashMap<>();
        args.put("language", Arrays.asList("ru", "ru-RU"));

        boolean result = languagePredicate.evaluate(context, new PredicateArguments(args));
        assertFalse(result);
    }

    @Test
    void testLanguagePredicateWithEmptyLanguageList() {
        RequestContext context = new RequestContext(LocalDateTime.now(), "en-US", null);

        Map<String, Object> args = new HashMap<>();
        args.put("language", Collections.emptyList());

        boolean result = languagePredicate.evaluate(context, new PredicateArguments(args));
        assertFalse(result);
    }
}
