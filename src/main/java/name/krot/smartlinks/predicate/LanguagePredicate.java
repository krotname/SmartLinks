package name.krot.smartlinks.predicate;

import java.util.List;
import java.util.Map;

public class LanguagePredicate implements Predicate {
    @Override
    public boolean evaluate(RequestContext context, Map<String, Object> args) {
        List<String> languages = (List<String>) args.get("language");
        return languages.contains(context.getAcceptLanguage());
    }
}
