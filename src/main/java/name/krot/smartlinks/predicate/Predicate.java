package name.krot.smartlinks.predicate;

public interface Predicate {
    String name();

    boolean evaluate(RequestContext context, PredicateArguments arguments);

    default void validateArguments(PredicateArguments arguments) {
        // Predicates without configurable arguments can keep the default implementation.
    }
}
