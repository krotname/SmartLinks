package name.krot.smartlinks.predicate;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static name.krot.smartlinks.support.SmartLinksTestFixtures.languageArguments;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.requestContext;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class LanguagePredicateTest {

    private final LanguagePredicate languagePredicate = new LanguagePredicate();

    @Test
    void testLanguageMatches() {
        boolean result = languagePredicate.evaluate(
                requestContext("ru-RU"),
                languageArguments(Arrays.asList("ru", "ru-RU"))
        );
        assertTrue(result);
    }

    @Test
    void testLanguageDoesNotMatch() {
        boolean result = languagePredicate.evaluate(
                requestContext("en-US"),
                languageArguments(Arrays.asList("ru", "ru-RU"))
        );
        assertFalse(result);
    }

    @Test
    void testLanguagePredicateWithEmptyLanguageList() {
        boolean result = languagePredicate.evaluate(
                requestContext("en-US"),
                languageArguments(Collections.emptyList())
        );
        assertFalse(result);
    }
}
