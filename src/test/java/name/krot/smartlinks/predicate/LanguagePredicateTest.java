package name.krot.smartlinks.predicate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


class LanguagePredicateTest {

    private final LanguagePredicate languagePredicate = new LanguagePredicate();

    @Test
    void testLanguageMatches() {
        RequestContext context = new RequestContext();
        context.setAcceptLanguage("ru-RU");

        Map<String, Object> args = new HashMap<>();
        args.put("language", Arrays.asList("ru", "ru-RU"));

        boolean result = languagePredicate.evaluate(context, args);
        assertTrue(result);
    }

    @Test
    void testLanguageDoesNotMatch() {
        RequestContext context = new RequestContext();
        context.setAcceptLanguage("en-US");

        Map<String, Object> args = new HashMap<>();
        args.put("language", Arrays.asList("ru", "ru-RU"));

        boolean result = languagePredicate.evaluate(context, args);
        assertFalse(result);
    }

    @Test
    void testLanguagePredicateWithEmptyLanguageList() {
        RequestContext context = new RequestContext();
        context.setAcceptLanguage("en-US");

        Map<String, Object> args = new HashMap<>();
        args.put("language", Collections.emptyList());

        boolean result = languagePredicate.evaluate(context, args);
        assertFalse(result);
    }
}