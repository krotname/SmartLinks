package name.krot.smartlinks.predicate;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static name.krot.smartlinks.support.SmartLinksTestFixtures.languageArguments;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.requestContext;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void parsesWeightedAcceptLanguageHeader() {
        boolean result = languagePredicate.evaluate(
                requestContext("en-US;q=0.5, ru-RU;q=0.9, ru;q=0.8"),
                languageArguments(Arrays.asList("ru", "ru-RU"))
        );

        assertTrue(result);
    }

    @Test
    void malformedOrMissingAcceptLanguageDoesNotBreakRedirectResolution() {
        assertFalse(languagePredicate.evaluate(
                requestContext("not a valid language header;="),
                languageArguments(Arrays.asList("ru", "ru-RU"))));
        assertFalse(languagePredicate.evaluate(
                requestContext(null),
                languageArguments(Arrays.asList("ru", "ru-RU"))));
    }

    @Test
    void rejectsIllFormedConfiguredLanguageTag() {
        assertThrows(IllegalArgumentException.class,
                () -> languagePredicate.validateArguments(languageArguments(List.of("en-@"))));
    }
}
