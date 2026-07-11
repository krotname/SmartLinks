package name.krot.smartlinks.service;

import static name.krot.smartlinks.support.SmartLinksTestFixtures.fallbackRule;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.rule;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.smartLink;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import name.krot.smartlinks.model.SmartLink;
import name.krot.smartlinks.predicate.DateRangePredicate;
import name.krot.smartlinks.predicate.DeviceTypePredicate;
import name.krot.smartlinks.predicate.LanguagePredicate;
import name.krot.smartlinks.predicate.PredicateFactoryImpl;
import name.krot.smartlinks.predicate.UserAgentDeviceTypeResolver;
import org.junit.jupiter.api.Test;

class SmartLinkDefinitionValidatorTest {

    private final SmartLinkDefinitionValidator validator = new SmartLinkDefinitionValidator(
            new PredicateFactoryImpl(List.of(
                    new DateRangePredicate(),
                    new LanguagePredicate(),
                    new DeviceTypePredicate(new UserAgentDeviceTypeResolver()))));

    @Test
    void acceptsValidAndFallbackRules() {
        assertDoesNotThrow(() -> validator.validate(smartLink(
                rule(List.of("Language"), "https://example.com", Map.of("language", List.of("ru-RU"))),
                fallbackRule("https://example.com/fallback"))));
    }

    @Test
    void rejectsUnknownDuplicateAndInvalidPredicateArguments() {
        assertThrows(IllegalArgumentException.class, () -> validator.validate(smartLink(
                rule(List.of("Unknown"), "https://example.com", Map.of()))));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(smartLink(
                rule(List.of("Language", "Language"), "https://example.com",
                        Map.of("language", List.of("ru"))))));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(smartLink(
                rule(List.of("DateRange"), "https://example.com", Map.of(
                        "startWith", "2026-12-01T00:00:00",
                        "endWith", "2026-01-01T00:00:00")))));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(smartLink(
                rule(List.of("DeviceType"), "https://example.com",
                        Map.of("devices", List.of("Console"))))));
    }

    @Test
    void rejectsInvalidStructureOutsideHttpBoundary() {
        SmartLink invalid = smartLink(fallbackRule("javascript:alert(1)"));
        invalid.setId("bad/id");

        assertThrows(IllegalArgumentException.class, () -> validator.validate(invalid));
    }

    @Test
    void rejectsUnreachableRuleAfterFallback() {
        assertThrows(IllegalArgumentException.class, () -> validator.validate(smartLink(
                fallbackRule("https://example.com/fallback"),
                rule(List.of("Language"), "https://example.com/ru",
                        Map.of("language", List.of("ru"))))));
    }

    @Test
    void rejectsIllFormedDateAsClientDefinitionError() {
        assertThrows(IllegalArgumentException.class, () -> validator.validate(smartLink(
                rule(List.of("DateRange"), "https://example.com", Map.of(
                        "startWith", "tomorrow",
                        "endWith", "later")))));
    }
}
