package name.krot.smartlinks.controller;

import jakarta.servlet.http.HttpServletRequest;
import name.krot.smartlinks.predicate.RequestContext;

public interface RequestContextFactory {
    RequestContext from(HttpServletRequest request);
}
