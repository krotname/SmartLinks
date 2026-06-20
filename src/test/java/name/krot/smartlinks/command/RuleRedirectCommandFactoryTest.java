package name.krot.smartlinks.command;

import name.krot.smartlinks.model.Rule;
import name.krot.smartlinks.model.SmartLink;
import name.krot.smartlinks.service.PredicateRuleEvaluator;
import name.krot.smartlinks.service.RedirectUriResolver;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class RuleRedirectCommandFactoryTest {

    private final RuleRedirectCommandFactory factory = new RuleRedirectCommandFactory(
            mock(PredicateRuleEvaluator.class),
            mock(RedirectUriResolver.class)
    );

    @Test
    void createsOneCommandPerRule() {
        SmartLink smartLink = new SmartLink();
        smartLink.setId("smartlink123");
        smartLink.setRules(List.of(rule("https://otus.ru/ru"), rule("https://otus.ru/default")));

        List<RedirectCommand> commands = factory.createCommands(smartLink);

        assertEquals(2, commands.size());
    }

    private static Rule rule(String redirectTo) {
        Rule rule = new Rule();
        rule.setPredicates(List.of());
        rule.setRedirectTo(redirectTo);
        return rule;
    }
}
