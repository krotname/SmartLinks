package name.krot.smartlinks.predicate;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateRangePredicateTest {

    private final DateRangePredicate dateRangePredicate = new DateRangePredicate();

    @Test
    void testDateWithinRange() {
        RequestContext context = new RequestContext();
        context.setRequestTime(LocalDateTime.of(2024, 11, 15, 12, 0));

        Map<String, Object> args = new HashMap<>();
        args.put("startWith", "2024-11-01T00:00:00");
        args.put("endWith", "2024-12-01T00:00:00");

        boolean result = dateRangePredicate.evaluate(context, args);
        assertTrue(result);
    }

    @Test
    void testDateBeforeRange() {
        RequestContext context = new RequestContext();
        context.setRequestTime(LocalDateTime.of(2024, 10, 31, 23, 59));

        Map<String, Object> args = new HashMap<>();
        args.put("startWith", "2024-11-01T00:00:00");
        args.put("endWith", "2024-12-01T00:00:00");

        boolean result = dateRangePredicate.evaluate(context, args);
        assertFalse(result);
    }

    @Test
    void testDateAfterRange() {
        RequestContext context = new RequestContext();
        context.setRequestTime(LocalDateTime.of(2024, 12, 1, 0, 1));

        Map<String, Object> args = new HashMap<>();
        args.put("startWith", "2024-11-01T00:00:00");
        args.put("endWith", "2024-12-01T00:00:00");

        boolean result = dateRangePredicate.evaluate(context, args);
        assertFalse(result);
    }

    @Test
    void testDateRangePredicateWithInvalidArgs() {
        RequestContext context = new RequestContext();
        context.setRequestTime(LocalDateTime.now());

        Map<String, Object> args = new HashMap<>();
        args.put("startWith", "invalid date");
        args.put("endWith", "invalid date");

        assertThrows(DateTimeParseException.class, () -> {
            dateRangePredicate.evaluate(context, args);
        });
    }
}