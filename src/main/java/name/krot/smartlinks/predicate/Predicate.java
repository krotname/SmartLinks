package name.krot.smartlinks.predicate;

public interface Predicate {
    String name();

    boolean evaluate(RequestContext context, PredicateArguments arguments);
}
