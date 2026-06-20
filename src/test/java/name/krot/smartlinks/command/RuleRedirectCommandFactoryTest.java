package name.krot.smartlinks.command;

import name.krot.smartlinks.model.Rule;
import name.krot.smartlinks.model.SmartLink;
import name.krot.smartlinks.service.PredicateRuleEvaluator;
import name.krot.smartlinks.service.RedirectUriResolver;
import org.junit.jupiter.api.Test;

import java.util.List;

import static name.krot.smartlinks.support.SmartLinksTestFixtures.DEFAULT_REDIRECT_URL;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.RU_REDIRECT_URL;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.fallbackRule;
import static name.krot.smartlinks.support.SmartLinksTestFixtures.smartLink;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class RuleRedirectCommandFactoryTest {

    private final RuleRedirectCommandFactory factory = new RuleRedirectCommandFactory(
            mock(PredicateRuleEvaluator.class),
            mock(RedirectUriResolver.class)
    );

    @Test
    void createsOneCommandPerRule() {
        SmartLink smartLink = smartLink(fallbackRule(RU_REDIRECT_URL), fallbackRule(DEFAULT_REDIRECT_URL));

        List<RedirectCommand> commands = factory.createCommands(smartLink);

        assertEquals(2, commands.size());
    }
}
