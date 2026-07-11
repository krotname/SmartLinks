package name.krot.smartlinks.predicate;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Component
public class DateRangePredicate implements Predicate {

    @Override
    public String name() {
        return "DateRange";
    }

    @Override
    public boolean evaluate(RequestContext context, PredicateArguments arguments) {
        DateRange range = range(arguments);
        LocalDateTime start = range.start();
        LocalDateTime end = range.end();
        return !context.requestTime().isBefore(start) && !context.requestTime().isAfter(end);
    }

    @Override
    public void validateArguments(PredicateArguments arguments) {
        range(arguments);
    }

    private static DateRange range(PredicateArguments arguments) {
        LocalDateTime start;
        LocalDateTime end;
        try {
            start = LocalDateTime.parse(arguments.getString("startWith"));
            end = LocalDateTime.parse(arguments.getString("endWith"));
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    "DateRange startWith and endWith must be ISO-8601 local date-times", exception);
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("DateRange endWith must not be before startWith");
        }
        return new DateRange(start, end);
    }

    private record DateRange(LocalDateTime start, LocalDateTime end) {
    }
}
