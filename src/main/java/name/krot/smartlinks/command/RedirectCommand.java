package name.krot.smartlinks.command;

import name.krot.smartlinks.predicate.RequestContext;
import name.krot.smartlinks.predicate.Rule;
import org.springframework.http.ResponseEntity;

public abstract class RedirectCommand implements Command<ResponseEntity<?>> {
    protected final RequestContext context;
    protected final Rule rule;

    public RedirectCommand(RequestContext context, Rule rule) {
        this.context = context;
        this.rule = rule;
    }
}
