package name.krot.smartlinks.predicate;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.IllformedLocaleException;

@Component
public class LanguagePredicate implements Predicate {

    @Override
    public String name() {
        return "Language";
    }

    @Override
    public boolean evaluate(RequestContext context, PredicateArguments arguments) {
        List<String> languages = arguments.getStringList("language");
        String acceptLanguage = context.acceptLanguage();
        if (acceptLanguage == null || acceptLanguage.isBlank() || languages.isEmpty()) {
            return false;
        }
        try {
            List<Locale.LanguageRange> requested = Locale.LanguageRange.parse(acceptLanguage);
            return !Locale.filterTags(requested, languages).isEmpty();
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    @Override
    public void validateArguments(PredicateArguments arguments) {
        List<String> languages = arguments.getStringList("language");
        if (languages.isEmpty()) {
            throw new IllegalArgumentException("Language predicate requires at least one language tag");
        }
        for (String language : languages) {
            if (!isWellFormedLanguageTag(language)) {
                throw new IllegalArgumentException("Invalid language tag: " + language);
            }
        }
    }

    private static boolean isWellFormedLanguageTag(String language) {
        if (language == null || language.isBlank() || !language.equals(language.trim())) {
            return false;
        }
        try {
            new Locale.Builder().setLanguageTag(language).build();
            return true;
        } catch (IllformedLocaleException exception) {
            return false;
        }
    }
}
