package name.krot.smartlinks.predicate;

@FunctionalInterface
public interface PredicateFactory {
    Predicate createPredicate(String name);
}