package name.krot.smartlinks.predicate;

import java.time.LocalDateTime;
import java.util.Map;

public class DateRangePredicate implements Predicate {
    public boolean evaluate(RequestContext context, Map<String, Object> args) {
        LocalDateTime start = LocalDateTime.parse((String) args.get("startWith"));
        LocalDateTime end = LocalDateTime.parse((String) args.get("endWith"));
        return !context.getRequestTime().isBefore(start) && !context.getRequestTime().isAfter(end);
    }
}
