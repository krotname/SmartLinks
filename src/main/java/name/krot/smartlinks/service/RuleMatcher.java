package name.krot.smartlinks.service;

import name.krot.smartlinks.model.SmartLink;
import name.krot.smartlinks.predicate.RequestContext;

import java.net.URI;
import java.util.Optional;

public interface RuleMatcher {
    Optional<URI> findRedirect(SmartLink smartLink, RequestContext context);
}
