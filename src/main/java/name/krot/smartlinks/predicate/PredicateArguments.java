package name.krot.smartlinks.predicate;

import java.util.List;
import java.util.Map;

public final class PredicateArguments {

    private final Map<String, Object> values;

    public PredicateArguments(Map<String, Object> values) {
        this.values = values == null ? Map.of() : Map.copyOf(values);
    }

    public String getString(String key) {
        Object value = require(key);
        if (value instanceof String stringValue) {
            return stringValue;
        }
        throw new IllegalArgumentException("Predicate argument '%s' must be a string".formatted(key));
    }

    public List<String> getStringList(String key) {
        Object value = require(key);
        if (value instanceof List<?> listValue && listValue.stream().allMatch(String.class::isInstance)) {
            return listValue.stream().map(String.class::cast).toList();
        }
        throw new IllegalArgumentException("Predicate argument '%s' must be a list of strings".formatted(key));
    }

    private Object require(String key) {
        if (!values.containsKey(key)) {
            throw new IllegalArgumentException("Missing predicate argument '%s'".formatted(key));
        }
        return values.get(key);
    }
}
