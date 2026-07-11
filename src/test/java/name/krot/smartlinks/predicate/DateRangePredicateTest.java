package name.krot.smartlinks.predicate;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static name.krot.smartlinks.support.SmartLinksTestFixtures.dateRangeArguments;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.requestContextAt;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateRangePredicateTest {

    private final DateRangePredicate dateRangePredicate = new DateRangePredicate();

    @Test
    void testDateWithinRange() {
        RequestContext context = requestContextAt(LocalDateTime.of(2024, 11, 15, 12, 0));

        boolean result = dateRangePredicate.evaluate(
                context,
                dateRangeArguments("2024-11-01T00:00:00", "2024-12-01T00:00:00")
        );
        assertTrue(result);
    }

    @Test
    void testDateBeforeRange() {
        RequestContext context = requestContextAt(LocalDateTime.of(2024, 10, 31, 23, 59));

        boolean result = dateRangePredicate.evaluate(
                context,
                dateRangeArguments("2024-11-01T00:00:00", "2024-12-01T00:00:00")
        );
        assertFalse(result);
    }

    @Test
    void testDateAfterRange() {
        RequestContext context = requestContextAt(LocalDateTime.of(2024, 12, 1, 0, 1));

        boolean result = dateRangePredicate.evaluate(
                context,
                dateRangeArguments("2024-11-01T00:00:00", "2024-12-01T00:00:00")
        );
        assertFalse(result);
    }

    @Test
    void testDateRangePredicateWithInvalidArgs() {
        RequestContext context = requestContextAt(LocalDateTime.now());

        assertThrows(IllegalArgumentException.class, () -> {
            dateRangePredicate.evaluate(context, dateRangeArguments("invalid date", "invalid date"));
        });
    }
}
