package name.krot.smartlinks.service;

import name.krot.smartlinks.model.Rule;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Optional;

@Service
public class RuleRedirectUriResolver implements RedirectUriResolver {

    @Override
    public Optional<URI> resolve(Rule rule) {
        return Optional.ofNullable(rule.getRedirectTo())
                .map(String::trim)
                .filter(Rule::isValidRedirectUrl)
                .map(URI::create);
    }
}
