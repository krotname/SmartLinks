package name.krot.smartlinks.command;

import name.krot.smartlinks.predicate.Predicate;
import name.krot.smartlinks.predicate.PredicateFactory;
import name.krot.smartlinks.predicate.RequestContext;
import name.krot.smartlinks.predicate.Rule;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class RuleCommand extends RedirectCommand {

    private final PredicateFactory predicateFactory;

    public RuleCommand(RequestContext context, Rule rule, PredicateFactory predicateFactory) {
        super(context, rule);
        this.predicateFactory = predicateFactory;
    }

    @Override
    public ResponseEntity<?> execute() {
        List<PredicateCommand> predicateCommands = new ArrayList<>();
        for (String predicateName : rule.getPredicates()) {
            Predicate predicate = predicateFactory.createPredicate(predicateName);
            predicateCommands.add(new PredicateCommandImpl(predicate, context, rule.getArgs()));
        }

        boolean allPredicatesTrue = predicateCommands.stream().allMatch(PredicateCommand::execute);

        if (allPredicatesTrue) {
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(rule.getRedirectTo()));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }

        return null;
    }
}
