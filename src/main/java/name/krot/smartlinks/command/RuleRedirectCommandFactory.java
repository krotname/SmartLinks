package name.krot.smartlinks.command;

import lombok.RequiredArgsConstructor;
import name.krot.smartlinks.model.Rule;
import name.krot.smartlinks.model.SmartLink;
import name.krot.smartlinks.service.PredicateRuleEvaluator;
import name.krot.smartlinks.service.RedirectUriResolver;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RuleRedirectCommandFactory implements RedirectCommandFactory {

    private final PredicateRuleEvaluator predicateRuleEvaluator;
    private final RedirectUriResolver redirectUriResolver;

    @Override
    public List<RedirectCommand> createCommands(SmartLink smartLink) {
        return smartLink.getRules().stream()
                .map(this::createCommand)
                .toList();
    }

    private RedirectCommand createCommand(Rule rule) {
        return new RuleRedirectCommand(rule, predicateRuleEvaluator, redirectUriResolver);
    }
}
