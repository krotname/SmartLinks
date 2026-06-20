package name.krot.smartlinks.service;

import name.krot.smartlinks.predicate.RequestContext;

import java.net.URI;

public interface RedirectResolver {
    URI resolveRedirect(String smartLinkId, RequestContext context);
}
