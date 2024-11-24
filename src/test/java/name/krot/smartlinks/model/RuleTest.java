package name.krot.smartlinks.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

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
}