package name.krot.smartlinks.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import name.krot.smartlinks.model.Rule;
import name.krot.smartlinks.model.SmartLink;
import name.krot.smartlinks.predicate.Predicate;
import name.krot.smartlinks.predicate.PredicateArguments;
import name.krot.smartlinks.predicate.PredicateFactory;
import org.springframework.stereotype.Component;

@Component
public class SmartLinkDefinitionValidator {

    private final PredicateFactory predicateFactory;

    public SmartLinkDefinitionValidator(PredicateFactory predicateFactory) {
        this.predicateFactory = predicateFactory;
    }

    public void validate(SmartLink smartLink) {
        if (smartLink == null) {
            throw new IllegalArgumentException("Smart Link cannot be null");
        }
        if (!SmartLink.isValidId(smartLink.getId())) {
            throw new IllegalArgumentException("Smart Link id contains unsupported characters or is too long");
        }
        List<Rule> rules = smartLink.getRules();
        if (rules == null || rules.isEmpty()) {
            throw new IllegalArgumentException("Rules list cannot be empty");
        }
        if (rules.size() > SmartLink.MAX_RULES) {
            throw new IllegalArgumentException("Rules list cannot exceed " + SmartLink.MAX_RULES + " entries");
        }
        for (int index = 0; index < rules.size(); index++) {
            validateRule(rules.get(index), index);
            if (rules.get(index).getPredicates().isEmpty() && index != rules.size() - 1) {
                throw new IllegalArgumentException("Fallback rule must be the final rule");
            }
        }
    }

    private void validateRule(Rule rule, int index) {
        if (rule == null) {
            throw new IllegalArgumentException("Rule at index " + index + " cannot be null");
        }
        if (!Rule.isValidRedirectUrl(rule.getRedirectTo())) {
            throw new IllegalArgumentException("Rule at index " + index + " has an invalid redirect URL");
        }
        if (rule.getPredicates() == null || rule.getArgs() == null) {
            throw new IllegalArgumentException("Rule predicates and args cannot be null");
        }
        if (rule.getPredicates().size() > Rule.MAX_PREDICATES) {
            throw new IllegalArgumentException(
                    "Rule predicates cannot exceed " + Rule.MAX_PREDICATES + " entries");
        }
        if (rule.getArgs().size() > Rule.MAX_ARGUMENTS) {
            throw new IllegalArgumentException("Rule args cannot exceed " + Rule.MAX_ARGUMENTS + " entries");
        }
        PredicateArguments arguments = new PredicateArguments(rule.getArgs());
        Set<String> names = new HashSet<>();
        for (String name : rule.getPredicates()) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Predicate name cannot be blank");
            }
            if (!names.add(name)) {
                throw new IllegalArgumentException("Duplicate predicate: " + name);
            }
            Predicate predicate = predicateFactory.createPredicate(name);
            predicate.validateArguments(arguments);
        }
    }
}
