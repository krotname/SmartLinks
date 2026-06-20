package name.krot.smartlinks.command;

import name.krot.smartlinks.model.Rule;
import name.krot.smartlinks.predicate.RequestContext;
import name.krot.smartlinks.service.PredicateRuleEvaluator;
import name.krot.smartlinks.service.RedirectUriResolver;

import java.net.URI;
import java.util.Optional;

public class RuleRedirectCommand implements RedirectCommand {

    private final Rule rule;
    private final PredicateRuleEvaluator predicateRuleEvaluator;
    private final RedirectUriResolver redirectUriResolver;

    public RuleRedirectCommand(
            Rule rule,
            PredicateRuleEvaluator predicateRuleEvaluator,
            RedirectUriResolver redirectUriResolver
    ) {
        this.rule = rule;
        this.predicateRuleEvaluator = predicateRuleEvaluator;
        this.redirectUriResolver = redirectUriResolver;
    }

    @Override
    public Optional<URI> execute(RequestContext context) {
        return Optional.of(rule)
                .filter(currentRule -> predicateRuleEvaluator.matches(currentRule, context))
                .flatMap(redirectUriResolver::resolve);
    }
}
