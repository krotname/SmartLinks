package name.krot.smartlinks.predicate;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LanguagePredicate implements Predicate {

    @Override
    public String name() {
        return "Language";
    }

    @Override
    public boolean evaluate(RequestContext context, PredicateArguments arguments) {
        List<String> languages = arguments.getStringList("language");
        return languages.contains(context.acceptLanguage());
    }
}
