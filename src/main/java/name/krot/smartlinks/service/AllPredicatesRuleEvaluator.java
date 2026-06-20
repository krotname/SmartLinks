package name.krot.smartlinks.service;

import lombok.RequiredArgsConstructor;
import name.krot.smartlinks.model.Rule;
import name.krot.smartlinks.predicate.PredicateArguments;
import name.krot.smartlinks.predicate.PredicateFactory;
import name.krot.smartlinks.predicate.RequestContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AllPredicatesRuleEvaluator implements PredicateRuleEvaluator {

    private final PredicateFactory predicateFactory;

    @Override
    public boolean matches(Rule rule, RequestContext context) {
        PredicateArguments arguments = new PredicateArguments(rule.getArgs());
        return rule.getPredicates().stream()
                .map(predicateFactory::createPredicate)
                .allMatch(predicate -> predicate.evaluate(context, arguments));
    }
}
