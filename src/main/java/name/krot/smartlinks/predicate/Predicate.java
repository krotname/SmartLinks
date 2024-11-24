package name.krot.smartlinks.predicate;

import java.util.Map;

@FunctionalInterface
public interface Predicate {
    boolean evaluate(RequestContext context, Map<String, Object> args);
}