package name.krot.smartlinks.command;

import name.krot.smartlinks.predicate.RequestContext;

import java.net.URI;
import java.util.Optional;

public interface RedirectCommand {
    Optional<URI> execute(RequestContext context);
}
