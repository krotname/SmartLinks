package name.krot.smartlinks.command;

import name.krot.smartlinks.predicate.RequestContext;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public interface RedirectCommandChain {
    Optional<URI> execute(List<RedirectCommand> commands, RequestContext context);
}
