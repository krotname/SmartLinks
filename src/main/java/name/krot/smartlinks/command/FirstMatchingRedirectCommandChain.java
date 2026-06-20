package name.krot.smartlinks.command;

import name.krot.smartlinks.predicate.RequestContext;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Service
public class FirstMatchingRedirectCommandChain implements RedirectCommandChain {

    @Override
    public Optional<URI> execute(List<RedirectCommand> commands, RequestContext context) {
        return commands.stream()
                .map(command -> command.execute(context))
                .flatMap(Optional::stream)
                .findFirst();
    }
}
