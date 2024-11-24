package name.krot.smartlinks.command;

import name.krot.smartlinks.model.RequestData;
import name.krot.smartlinks.model.Url;
import name.krot.smartlinks.predicate.PredicateFactory;
import name.krot.smartlinks.predicate.RequestContext;
import name.krot.smartlinks.predicate.Rule;
import name.krot.smartlinks.predicate.SmartLink;
import name.krot.smartlinks.service.UrlService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class CommandFactory {
    private final PredicateFactory predicateFactory;

    public CommandFactory(PredicateFactory predicateFactory) {
        this.predicateFactory = predicateFactory;
    }

    public List<Command<ResponseEntity<?>>> createCommands(RequestContext context, SmartLink smartLink) {
        List<Command<ResponseEntity<?>>> commands = new ArrayList<>();
        for (Rule rule : smartLink.getRules()) {
            commands.add(new RuleCommand(context, rule, predicateFactory));
        }
        return commands;
    }
}
