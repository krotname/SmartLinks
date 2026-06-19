package name.krot.smartlinks.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleTest {

    @Test
    void testRuleGettersAndSetters() {
        Rule rule = new Rule();
        rule.setPredicates(Arrays.asList("Language", "DateRange"));
        rule.setRedirectTo("https://otus.ru");
        rule.setArgs(new HashMap<>());

        assertEquals(2, rule.getPredicates().size());
        assertEquals("https://otus.ru", rule.getRedirectTo());
        assertNotNull(rule.getArgs());
    }

    @Test
    void validatesRedirectUrlHasHttpSchemeAndHost() {
        assertTrue(Rule.isValidRedirectUrl("https://otus.ru/path"));
        assertTrue(Rule.isValidRedirectUrl("http://localhost:8080/path"));

        assertFalse(Rule.isValidRedirectUrl("javascript:alert(1)"));
        assertFalse(Rule.isValidRedirectUrl("https:otus.ru/no-host"));
        assertFalse(Rule.isValidRedirectUrl("https://"));
        assertFalse(Rule.isValidRedirectUrl("https://trusted.example@evil.example/path"));
    }
}
