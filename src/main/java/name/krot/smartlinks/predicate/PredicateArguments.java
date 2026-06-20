package name.krot.smartlinks.predicate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class PredicateArguments {

    private final Map<String, Object> values;

    public PredicateArguments(Map<String, Object> values) {
        this.values = values == null ? Map.of() : Map.copyOf(values);
    }

    public String getString(String key) {
        return Optional.ofNullable(require(key))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Predicate argument '%s' must be a string".formatted(key)
                ));
    }

    public List<String> getStringList(String key) {
        return Optional.ofNullable(require(key))
                .filter(value -> value instanceof List<?>)
                .map(value -> (List<?>) value)
                .filter(listValue -> listValue.stream().allMatch(String.class::isInstance))
                .map(listValue -> listValue.stream().map(String.class::cast).toList())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Predicate argument '%s' must be a list of strings".formatted(key)
                ));
    }

    private Object require(String key) {
        return Optional.of(values)
                .filter(currentValues -> currentValues.containsKey(key))
                .map(currentValues -> currentValues.get(key))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Missing predicate argument '%s'".formatted(key)
                ));
    }
}
