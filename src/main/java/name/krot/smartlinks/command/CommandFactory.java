package name.krot.smartlinks.command;

import name.krot.smartlinks.predicate.PredicateFactory;
import name.krot.smartlinks.predicate.RequestContext;
import name.krot.smartlinks.model.Rule;
import name.krot.smartlinks.model.SmartLink;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
