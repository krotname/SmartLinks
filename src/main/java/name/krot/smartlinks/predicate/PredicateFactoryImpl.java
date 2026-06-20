package name.krot.smartlinks.predicate;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PredicateFactoryImpl implements PredicateFactory {

    private final Map<String, Predicate> predicatesByName;

    public PredicateFactoryImpl(List<Predicate> predicates) {
        this.predicatesByName = predicates.stream()
                .collect(Collectors.toUnmodifiableMap(Predicate::name, Function.identity()));
    }

    @Override
    public Predicate createPredicate(String name) {
        return Optional.ofNullable(predicatesByName.get(name))
                .orElseThrow(() -> new IllegalArgumentException("Unknown predicate: " + name));
    }
}
