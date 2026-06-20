package name.krot.smartlinks.service;

import name.krot.smartlinks.model.Rule;

import java.net.URI;
import java.util.Optional;

public interface RedirectUriResolver {
    Optional<URI> resolve(Rule rule);
}
