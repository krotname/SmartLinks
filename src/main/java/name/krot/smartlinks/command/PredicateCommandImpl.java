package name.krot.smartlinks.command;

import name.krot.smartlinks.predicate.Predicate;
import name.krot.smartlinks.predicate.RequestContext;

import java.util.Map;

public class PredicateCommandImpl implements PredicateCommand {
    private final Predicate predicate;
    private final RequestContext context;
    private final Map<String, Object> args;

    public PredicateCommandImpl(Predicate predicate, RequestContext context, Map<String, Object> args) {
        this.predicate = predicate;
        this.context = context;
        this.args = args;
    }

    @Override
    public boolean execute() {
        return predicate.evaluate(context, args);
    }
}
