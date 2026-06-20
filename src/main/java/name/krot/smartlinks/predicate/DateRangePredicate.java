package name.krot.smartlinks.predicate;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DateRangePredicate implements Predicate {

    @Override
    public String name() {
        return "DateRange";
    }

    @Override
    public boolean evaluate(RequestContext context, PredicateArguments arguments) {
        LocalDateTime start = LocalDateTime.parse(arguments.getString("startWith"));
        LocalDateTime end = LocalDateTime.parse(arguments.getString("endWith"));
        return !context.requestTime().isBefore(start) && !context.requestTime().isAfter(end);
    }
}
