package name.krot.smartlinks.service;

import name.krot.smartlinks.model.Rule;
import name.krot.smartlinks.predicate.RequestContext;

public interface PredicateRuleEvaluator {
    boolean matches(Rule rule, RequestContext context);
}
