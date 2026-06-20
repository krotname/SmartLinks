package name.krot.smartlinks.service;

import lombok.RequiredArgsConstructor;
import name.krot.smartlinks.model.Rule;
import name.krot.smartlinks.model.SmartLink;
import name.krot.smartlinks.predicate.PredicateArguments;
import name.krot.smartlinks.predicate.PredicateFactory;
import name.krot.smartlinks.predicate.RequestContext;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PredicateRuleMatcher implements RuleMatcher {

    private final PredicateFactory predicateFactory;

    @Override
    public Optional<URI> findRedirect(SmartLink smartLink, RequestContext context) {
        for (Rule rule : smartLink.getRules()) {
            PredicateArguments arguments = new PredicateArguments(rule.getArgs());
            boolean matches = rule.getPredicates().stream()
                    .map(predicateFactory::createPredicate)
                    .allMatch(predicate -> predicate.evaluate(context, arguments));

            if (matches && Rule.isValidRedirectUrl(rule.getRedirectTo())) {
                return Optional.of(URI.create(rule.getRedirectTo().trim()));
            }
        }
        return Optional.empty();
    }
}
