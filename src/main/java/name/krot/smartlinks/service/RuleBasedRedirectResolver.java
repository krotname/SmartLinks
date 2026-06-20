package name.krot.smartlinks.service;

import lombok.RequiredArgsConstructor;
import name.krot.smartlinks.exception.NoMatchingRuleException;
import name.krot.smartlinks.exception.SmartLinkNotFoundException;
import name.krot.smartlinks.model.SmartLink;
import name.krot.smartlinks.predicate.RequestContext;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class RuleBasedRedirectResolver implements RedirectResolver {

    private final SmartLinkService smartLinkService;
    private final RuleMatcher ruleMatcher;

    @Override
    public URI resolveRedirect(String smartLinkId, RequestContext context) {
        SmartLink smartLink = smartLinkService.findSmartLinkById(smartLinkId)
                .orElseThrow(() -> new SmartLinkNotFoundException("Smart Link not found"));

        return ruleMatcher.findRedirect(smartLink, context)
                .orElseThrow(() -> new NoMatchingRuleException("No matching rule found for this Smart Link"));
    }
}
